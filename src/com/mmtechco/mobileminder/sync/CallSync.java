package com.mmtechco.mobileminder.sync;

import java.io.IOException;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Sync the call data to the server.
 */
public class CallSync implements Runnable {
	private static final String TAG = ToolsBB
			.getSimpleClassName(CallSync.class);

	private int type = 11;
	private MMTools tools = ToolsBB.getInstance();

	public CallSync() {
		Logger.log(TAG, "Started");
	}

	/**
	 * Matches the device call time with the server call time. If the value the
	 * server holds is less than device call time, it sends call messages to the
	 * server.
	 */
	public void run() {
		Logger.log(TAG, "Running");
		Response response;
		Reply reply;
		try {
			response = Server.get(Registration.getRegID() + Server.separator + type);
			reply = new Reply(response.getContent());
		} catch (IOException e) {
			Logger.log(TAG, e.getMessage());
			return;
		} catch (Exception e) {
			Logger.log(TAG, e.getMessage());
			return;
		}
		Logger.log(TAG, "Contacted server. Reply: " + reply.getREST());
		Logger.log(TAG, "Calling code" + reply.getCallingCode());

		// Check if the reply contained a valid server command
		if (tools.isNumber(reply.getCallingCode())
				&& Integer.parseInt(reply.getCallingCode()) == type) {
			Logger.log(TAG, "Valid reply received");
			long serverTimestamp = 0;
			try {
				// Convert server timestamp to unix time
				serverTimestamp = tools.getDate(reply.getInfo());
			} catch (Exception e) {
				Logger.log(TAG, e.getMessage());
				return;
			}
			// Get the list of calls in the call log
			PhoneLogs phoneLog = PhoneLogs.getInstance();
			int numCalls = phoneLog
					.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);
			Logger.log(TAG, "Number of calls in call log: " + numCalls);

			// Loop through call list
			Logger.log(TAG, "Looping through call log");
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
					Logger.log(TAG, "Found new call entry");
					Logger.log(TAG, "lastServerTime:" + serverTimestamp
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
						Logger.log(TAG, callMessageHolder.toString());
						// Contact server with the call log entry
						try {
							response = Server.get(callMessageHolder.toString());
						} catch (IOException e) {
							Logger.log(TAG, e.getMessage());
						}
					}
				}
			}
		} else {
			Logger.log(TAG, "No valid reply received");
		}
		Logger.log(TAG, "Finished sync");
	}
}