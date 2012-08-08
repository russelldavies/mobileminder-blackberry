package com.mmtechco.mobileminder.monitor;

import java.util.Date;

import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors and registers call based events.
 */
public class CallMonitor extends AbstractPhoneListener {
	private static Logger logger = Logger.getLogger(CallMonitor.class);

	private static CallMonitor callMonitor;
	private static CallMessage callMessage;

	public static void start() {
		Phone.addPhoneListener(callMonitor = new CallMonitor());
		logger.info("Started");
	}
	
	public static void stop() {
		Phone.removePhoneListener(callMonitor);
	}

	public void callAnswered(int callId) {
		logger.info("Call answered");

		String number = Phone.getCall(callId).getPhoneNumber();
		//#endif
		String contactName = new PhoneCallLogID(number).getName();
		contactName = (contactName == null ? "" : contactName);
		
		callMessage = new CallMessage(number, contactName, false);
	}

	public void callInitiated(int callId) {
		logger.info("Call initiated");
		
		String number = Phone.getCall(callId).getPhoneNumber();
		String contactName = new PhoneCallLogID(number).getName();
		contactName = (contactName == null ? "" : contactName);
		
		callMessage = new CallMessage(number, contactName, true);
	}

	public void callEndedByUser(int callId) {
		logger.info("Call ended by user");

		callMessage.callEnded(CallMessage.FINISHED);
		ActivityLog.addMessage(callMessage);
	}

	public void callDisconnected(int callId) {
		logger.info("Call disconnected");

		callMessage.callEnded(CallMessage.DROPPED);
		ActivityLog.addMessage(callMessage);
	}

	public static class CallMessage extends Message {
		static final int OTHER = 0;
		static final int NO_ANSWER = 1;
		static final int DROPPED = 2;
		static final int FINISHED = 3;
		
		private Date callStart;
		private boolean outgoing;
		
		/**
		 * Message format:
		 * <ul>
		 * <li>Call timestamp
		 * <li>Phone number (received or dialled)
		 * <li>Contact name
		 * <li>Call duration
		 * <li>Direction: outgoing = true, incoming = false
		 * <li>Call end status (unanswered, finished, etc)
		 * </ul>
		 */
		public CallMessage(String number, String contactName, boolean outgoing) {
			super(Message.CALL);
			add(ToolsBB.getInstance().getDate());
			add(number);
			add(contactName);
			this.outgoing = outgoing;
			this.callStart = new Date();
		}
		
		public void callEnded(int endStatus) {
			add(String.valueOf((int) (new Date().getTime() - callStart.getTime()) / 1000));
			add(outgoing ? "true" : "false");
			add(String.valueOf(endStatus));
		}

		// Called from CallSync
		public CallMessage(String callTimeStamp, String number,
				String contactName, int callDuration, int status) {
			super(Message.CALL_SYNC, new String[] {
					callTimeStamp,
					number,
					(contactName == null) ? "" : contactName,
					String.valueOf(callDuration),
					String.valueOf(status),
					String.valueOf(OTHER)
			});
		}
	}
}
