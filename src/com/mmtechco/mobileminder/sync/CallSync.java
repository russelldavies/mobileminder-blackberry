package com.mmtechco.mobileminder.sync;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;

/**
 * Sync the call data to the server.
 */
public class CallSync extends Thread {
	private static final String TAG = ToolsBB.getSimpleClassName(CallSync.class);

	private boolean finishedSync;
	private Server server;
	private int type = 11;
	private Logger logger = Logger.getInstance();
	private MMTools tools = ToolsBB.getInstance();

	public CallSync(Server _server) {
		logger.log(TAG, "Starting CallSync");
		finishedSync = false;
		server = _server;
	}

	/**
	 * Matches the device call time with the server call time. If server time is
	 * less than device call time, it sends call messages to server
	 */
	public void run() {
		logger.log(TAG, "Running CallSync");
		StringBuffer stringREST = new StringBuffer();
		stringREST.append(Registration.getRegID());
		stringREST.append(Tools.ServerQueryStringSeparator);
		stringREST.append(type);

		logger.log(TAG, "CallSync::Contacting Server...");
		Reply serverReply = server.contactServer(stringREST.toString());
		long lastServerTime;
		try {
			lastServerTime = Long.parseLong(serverReply.getInfo());
		} catch (NumberFormatException e) {
			lastServerTime = 0;
		}

		logger.log(TAG, "Callsync calling code:" + serverReply.getCallingCode());

		// Get the list of calls in the call log
		PhoneLogs phoneLog = PhoneLogs.getInstance();
		// Store the number of regular calls, and missed calls
		int numCalls = phoneLog.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);

		logger.log(TAG, "Number of calls in call log: " + numCalls);
		logger.log(TAG, "Last call time on server: " + lastServerTime);

		// Check if the reply contained a valid server command
		if (tools.isNumber(serverReply.getCallingCode())
				&& Integer.parseInt(serverReply.getCallingCode()) == type) {
			logger.log(TAG, "CallSync::Valid reply received");
			// Create object to store each calls details, one by one
			CallMonitor.CallMessage callMessageHolder = new CallMonitor(null).new CallMessage(true);
			// Loop through call list
			logger.log(TAG, "CallSync::Looping through call log...");
			for (int count = 0; count < numCalls; count++) {
				// Get the call log entry
				CallLog callLogEntry = phoneLog.callAt(count,
						PhoneLogs.FOLDER_NORMAL_CALLS);

				// Get the phone number from this Call Log entry
				PhoneCallLog callLog = (PhoneCallLog) phoneLog.callAt(count,
						PhoneLogs.FOLDER_NORMAL_CALLS);
				PhoneCallLogID phoneID = callLog.getParticipant();

				// Get the time when the call was made
				long callSystemTime = callLogEntry.getDate().getTime();

				// If there is a newer call in the log, than the last one the
				// server has...
				if (lastServerTime < Long.parseLong(tools
						.getDate(callSystemTime))) {
					logger.log(TAG, "CallSync::Found new call entry");
					logger.log(TAG, "CALL lastServerTime:" + lastServerTime
							+ " < " + callSystemTime + "callSystemTime");
					int StartCallState = callLogEntry.getStatus(); // <- is it
																	// inbound
																	// or
																	// outbound

					if (PhoneCallLog.TYPE_PLACED_CALL == StartCallState
							|| PhoneCallLog.TYPE_RECEIVED_CALL == StartCallState) {
						boolean outgoing = false;
						if (PhoneCallLog.TYPE_PLACED_CALL == StartCallState) {
							outgoing = true;
						}

						// Output data for DEBUG
						logger.log(
								TAG,
								"Message> "
										+ phoneID
												.getAddressBookFormattedNumber()
										+ " outgoing:" + outgoing + " Date: "
										+ tools.getDate(callSystemTime)
										+ " Duration: "
										+ callLogEntry.getDuration());

						// Add data to object, and subsequently the database.
						callMessageHolder.setMessage(
								phoneID.getAddressBookFormattedNumber(),
								outgoing, tools.getDate(callSystemTime),
								callLogEntry.getDuration());

						// Contact/Reply to server with the call log entry
						server.contactServer(callMessageHolder);
					}
					callMessageHolder.clearData();// reset only when the loop is
													// processing message
				}// end if check event VS server time
			} // end for()
		} // end if()
		else {
			logger.log(TAG, "CallSync::No valid reply received");
		}
		logger.log(TAG, "CallSync::Finished sync");
		finishedSync = true;
	}

	/**
	 * Return true if synchronisation is completed.
	 * 
	 * @return true if synchronisation is completed, false otherwise.
	 */
	public boolean syncComplete() {
		return finishedSync;
	}
}