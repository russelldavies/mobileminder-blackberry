package com.mmtechco.mobileminder.sync;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;

/**
 * Sync the call data to the server.
 */
public class CallSync implements Runnable {
	private static final String TAG = ToolsBB
			.getSimpleClassName(CallSync.class);

	private Server server;
	private int type = 11;
	private Logger logger = Logger.getInstance();
	private MMTools tools = ToolsBB.getInstance();

	public CallSync() {
		logger.log(TAG, "Started");
		this.server = new Server();
	}

	/**
	 * Matches the device call time with the server call time. If the value the
	 * server holds is less than device call time, it sends call messages to the
	 * server.
	 */
	public void run() {
		logger.log(TAG, "Running");
		Reply serverReply = server.contactServer(Registration.getRegID()
				+ Tools.ServerQueryStringSeparator + type);
		logger.log(TAG, "Contacted server. Reply: " + serverReply.getREST());
		logger.log(TAG, "Calling code" + serverReply.getCallingCode());

		// Check if the reply contained a valid server command
		if (tools.isNumber(serverReply.getCallingCode())
				&& Integer.parseInt(serverReply.getCallingCode()) == type) {
			logger.log(TAG, "Valid reply received");
			long serverTimestamp = 0;
			try {
				// Convert server timestamp to unix time
				serverTimestamp = tools.getDate(serverReply.getInfo());
			} catch (Exception e) {
				// TODO: add error to localdataaccess
				logger.log(TAG, e.getMessage());
				return;
			}
			// Get the list of calls in the call log
			PhoneLogs phoneLog = PhoneLogs.getInstance();
			int numCalls = phoneLog
					.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
			logger.log(TAG, "Number of calls in call log: " + numCalls);

			// Loop through call list
			logger.log(TAG, "Looping through call log");
			for (int index = 0; index < numCalls; index++) {
				// Create object to store details of each call
				CallMonitor.CallMessage callMessageHolder = new CallMonitor.CallMessage(true);
				// Get particular call and unix timestamp
				PhoneCallLog callLogEntry = (PhoneCallLog) phoneLog.callAt(
						index, PhoneLogs.FOLDER_NORMAL_CALLS);
				long callTimestamp = callLogEntry.getDate().getTime();
				// Get the phone number from this Call Log entry
				PhoneCallLogID callParticipant = callLogEntry.getParticipant();

				// If there is a newer call in the log, than the last one the
				// server has
				if (serverTimestamp < callTimestamp) {
					logger.log(TAG, "Found new call entry");
					logger.log(TAG, "lastServerTime:" + serverTimestamp
							+ "; callTimestamp" + callTimestamp);

					// Call must be inbound or outbound (not missed)
					int callType = callLogEntry.getStatus();
					if (PhoneCallLog.TYPE_PLACED_CALL == callType
							|| PhoneCallLog.TYPE_RECEIVED_CALL == callType) {
						boolean outgoing = false;
						if (PhoneCallLog.TYPE_PLACED_CALL == callType) {
							outgoing = true;
						}
						// Add data to object, and subsequently the database.
						callMessageHolder
								.setMessage(
										tools.getDate(callTimestamp),
										callParticipant
												.getAddressBookFormattedNumber(),
										callParticipant.getName(), callLogEntry
												.getDuration(), outgoing);
						logger.log(TAG, callMessageHolder.getREST());
						// Contact server with the call log entry
						server.contactServer(callMessageHolder);
					}
				}
			}
		} else {
			logger.log(TAG, "No valid reply received");
		}
		logger.log(TAG, "Finished sync");
	}
}