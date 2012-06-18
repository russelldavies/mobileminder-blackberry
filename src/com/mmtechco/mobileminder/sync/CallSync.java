package com.mmtechco.mobileminder.sync;

import java.io.IOException;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Sync the call data to the server.
 */
public class CallSync implements Runnable {
	private static Logger logger = Logger.getLogger(CallSync.class);
	private MMTools tools = ToolsBB.getInstance();

	/**
	 * Matches the device call time with the server call time. If the value the
	 * server holds is less than device call time, it sends call messages to the
	 * server.
	 */
	public void run() {
		try {
		logger.debug("Running");
		Response response;
		Reply.Regular reply;
		response = Server.get(Registration.getRegID() + Message.SEPARATOR
				+ Message.CALL_SYNC);
		reply = new Reply.Regular(response.getContent());
		logger.debug("Contacted server. Reply: " + reply.content);
		logger.debug("Calling code" + reply.type);

		// Check if the reply contained a valid server command
		if (reply.type != Message.CALL_SYNC) {
			logger.warn("No valid server reply");
			return;
		}
		logger.debug("Valid reply received");
		long serverTimestamp = 0;
		// Convert server timestamp to unix time
		serverTimestamp = tools.getDate(reply.info);
		// Get the list of calls in the call log
		PhoneLogs phoneLog = PhoneLogs.getInstance();
		int numCalls = phoneLog.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
		logger.debug("Number of calls in call log: " + numCalls);

		// Loop through call list
		logger.debug("Looping through call log");
		for (int index = 0; index < numCalls; index++) {
			// Get particular call and unix timestamp
			PhoneCallLog callLogEntry = (PhoneCallLog) phoneLog.callAt(index,
					PhoneLogs.FOLDER_NORMAL_CALLS);
			long callTimestamp = callLogEntry.getDate().getTime();
			// Get the phone number from this Call Log entry
			PhoneCallLogID callParticipant = callLogEntry.getParticipant();

			// If there is a newer call in the log, than the last one the
			// server has
			if (serverTimestamp < callTimestamp) {
				logger.debug("Found new call entry");
				logger.debug("lastServerTime:" + serverTimestamp
						+ "; callTimestamp" + callTimestamp);

				// Call must be inbound or outbound (not missed)
				int callType = callLogEntry.getStatus();
				if (PhoneCallLog.TYPE_PLACED_CALL == callType
						|| PhoneCallLog.TYPE_RECEIVED_CALL == callType) {
					boolean outgoing = false;
					if (PhoneCallLog.TYPE_PLACED_CALL == callType) {
						outgoing = true;
					}
					CallMonitor.CallMessage message = new CallMonitor.CallMessage(

					tools.getDate(callTimestamp),
							callParticipant.getAddressBookFormattedNumber(),
							callParticipant.getName(),
							callLogEntry.getDuration(), outgoing);

					// Contact server with the call log entry
						response = Server.get(message.toString());
				}
			}
		}
		logger.debug("Finished sync");
		} catch (IOException e) {
			logger.warn("Connection problem: " + e.getMessage());
		} catch (ParseException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		}
	}
}