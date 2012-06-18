package com.mmtechco.mobileminder.sync;

import java.io.IOException;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

/**
 * Sync the call data to the server by matching the device call time with the
 * server call time. If the value the server holds is less than device call
 * time, it sends call messages to the server.
 */
public class CallSync implements Runnable {
	private static Logger logger = Logger.getLogger(CallSync.class);
	private MMTools tools = ToolsBB.getInstance();

	public void run() {
		try {
			logger.info("Running");
			Response response = Server.get(new Message(Message.CALL_SYNC)
					.toString());
			Reply.Regular reply = new Reply.Regular(response.getContent());
			logger.debug("Contacted server. Reply: " + reply.content);

			// Check if the reply contained a valid server command
			if (reply.type != Message.CALL_SYNC) {
				logger.warn("No valid server reply");
				return;
			}

			// Get the list of calls in the call log
			PhoneLogs phoneLog = PhoneLogs.getInstance();
			int numCalls = phoneLog
					.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
			logger.debug("Number of calls in call log: " + numCalls);

			// Convert server timestamp to unix time
			long serverTimestamp = tools.getDate(reply.info);

			// Loop through call list backwards
			logger.debug("Looping through call log");
			for (int i = numCalls - 1; i >= 0; i--) {
				PhoneCallLog callLogEntry = (PhoneCallLog) phoneLog.callAt(i,
						PhoneLogs.FOLDER_NORMAL_CALLS);
				// Unix timestamp of call
				long callTimestamp = callLogEntry.getDate().getTime();
				// Phone number of call
				PhoneCallLogID callParticipant = callLogEntry.getParticipant();

				// Server timestamp must be less than last call timestamp and
				// calls must be placed or received (not missed)
				if (serverTimestamp < callTimestamp
						&& callLogEntry.getStatus() <= PhoneCallLog.TYPE_PLACED_CALL) {
					logger.debug("Found newer call entry: server:"
							+ serverTimestamp + "; call" + callTimestamp);
					// Passing the call status directly to the CallMessage works
					// because the server expects a '1' for placed or outgoing
					// and '0' for incoming/received
					CallMonitor.CallMessage message = new CallMonitor.CallMessage(
							tools.getDate(callTimestamp),
							callParticipant.getAddressBookFormattedNumber(),
							callParticipant.getName(),
							callLogEntry.getDuration(),
							callLogEntry.getStatus());

					// Contact server with the call log entry
					Server.get(message.toString());
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