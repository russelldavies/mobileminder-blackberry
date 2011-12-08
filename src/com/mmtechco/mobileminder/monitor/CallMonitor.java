package com.mmtechco.mobileminder.monitor;

import java.util.Date;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

/**
 * Monitors and registers call based events.
 */
public class CallMonitor extends AbstractPhoneListener implements Controllable {
	private static final String TAG = ToolsBB
			.getSimpleClassName(CallMonitor.class);

	private LocalDataWriter actLog;
	private Logger logger = Logger.getInstance();
	private CallMessage callMessage;

	public CallMonitor(LocalDataWriter inputAccess) {
		actLog = inputAccess;
		Phone.addPhoneListener(this);
		logger.log(TAG, "Started");
	}

	public void callAnswered(int callId) {
		logger.log(TAG, "Call answered");

		callMessage = new CallMessage();
		String number = Phone.getCall(callId).getPhoneNumber();
		String contactName = new PhoneCallLogID(number).getName();
		callMessage.callStarted(number, (contactName == null ? "" : contactName), true);
	}

	public void callInitiated(int callId) {
		logger.log(TAG, "Call initiated");
		
		callMessage = new CallMessage();
		String number = Phone.getCall(callId).getPhoneNumber();
		String contactName = new PhoneCallLogID(number).getName();
		callMessage.callStarted(number, (contactName == null ? "" : contactName), true);
	}

	public void callEndedByUser(int callId) {
		logger.log(TAG, "Call ended by user");

		callMessage.callEnded(CallEndStatus.FINISHED);
		actLog.addMessage(callMessage);
		logger.log(TAG, "Call message added to log");
	}

	public void callDisconnected(int callId) {
		logger.log(TAG, "Call disconnected");

		callMessage.callEnded(CallEndStatus.DROPPED);
		actLog.addMessage(callMessage);
		logger.log(TAG, "Call message added to log");
	}

	public boolean isTarget(COMMAND_TARGETS inputCOMMAND_TARGETS) {
		if (inputCOMMAND_TARGETS == COMMAND_TARGETS.CALL) {
			return true;
		} else {
			return false;
		}
	}

	public boolean processCommand(String[] arg0) {
		// TODO implement
		return true;
	}

	/**
	 * This class implements the message interface to hold call event messages.
	 */
	public static class CallMessage implements Message {
		private MMTools tools = ToolsBB.getInstance();
		private final String type;
		private String callTimeStamp;
		private String number;
		private String contactName;
		private int callDuration;
		private Date callStart;
		private Date callEnd;
		private boolean outgoing;
		private byte endStatus;

		/**
		 * Initialize a normal call message
		 */
		public CallMessage() {
			this(false);
		}

		/**
		 * Initialize a normal call message or a sync call message based on the
		 * boolean value passed to it.
		 * 
		 * @param sync
		 *            - true if sync message
		 */
		public CallMessage(boolean sync) {
			if (sync) {
				type = "11";
			} else {
				type = "01";
			}
		}

		public void callStarted(String number, String contactName, boolean outgoing) {
			callStart = new Date();
			callTimeStamp = tools.getDate();
			this.number = number;
			this.contactName = contactName;
			this.outgoing = outgoing;
		}
		
		public void callEnded(byte endStatus) {
			callEnd = new Date();
			callDuration = (int) (callEnd.getTime() - callStart.getTime()) / 1000;
			this.endStatus = endStatus;
		}

		// Called from CallSync
		public void setMessage(String callTimeStamp, String number, String contactName, int callDuration, boolean outgoing) {
			this.callTimeStamp = callTimeStamp;
			this.number = number;
			this.contactName = contactName == null ? "" : contactName;
			this.callDuration = callDuration;
			this.outgoing = outgoing;
			this.endStatus = CallEndStatus.OTHER;
		}

		/**
		 * This method retrieves the message formatted in to a single string
		 * value.
		 * <p>
		 * Call message consists of:
		 * <ul>
		 * <li>Registration Serial number.
		 * <li>Call message type (two digits number).<br>
		 * '11' is used If call message is synchronised and '01' is used if
		 * message is not synchronised.
		 * <li>Time when the call begins.
		 * <li>Phone Number ( received or dialled ).
		 * <li>Contact Name.
		 * <li>Call duration
		 * <li>Call start status ( Incoming or outgoing )
		 * <li>Call end status ( no-answered, finished etc )
		 * </ul>
		 * 
		 * @return a single string containing the entire message.
		 */
		public String getREST() {
			return Registration.getRegID() + Tools.ServerQueryStringSeparator
					+ type + Tools.ServerQueryStringSeparator + callTimeStamp
					+ Tools.ServerQueryStringSeparator + number
					+ Tools.ServerQueryStringSeparator + contactName
					+ Tools.ServerQueryStringSeparator + callDuration
					+ Tools.ServerQueryStringSeparator + outgoing
					+ Tools.ServerQueryStringSeparator + endStatus;
		}

		public String getTime() {
			return callTimeStamp;
		}

		public int getType() {
			return Integer.parseInt(type);
		}
	}

	/**
	 * This is a mock enumeration (enums don't exist in BB API) of the different
	 * ways in which a call can be ended.
	 */
	public class CallEndStatus {
		private CallEndStatus() {
		}

		public static final byte OTHER = 0;
		public static final byte NO_ANSWER = 1;
		public static final byte DROPPED = 2;
		public static final byte FINISHED = 3;
	}
}
