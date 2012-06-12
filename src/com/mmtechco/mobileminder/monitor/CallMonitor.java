//#preprocess
package com.mmtechco.mobileminder.monitor;

import java.util.Date;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

/**
 * Monitors and registers call based events.
 */
public class CallMonitor extends AbstractPhoneListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(CallMonitor.class);

	private CallMessage callMessage;

	public CallMonitor() {
		Phone.addPhoneListener(this);
		Logger.log(TAG, "Started");
	}

	public void callAnswered(int callId) {
		Logger.log(TAG, "Call answered");

		callMessage = new CallMessage();
		//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1
		String number = Phone.getCall(callId).getPhoneNumber();
		//#else
		String number = Phone.getCall(callId).getDisplayPhoneNumber();
		//#endif
		String contactName = new PhoneCallLogID(number).getName();
		callMessage.callStarted(number, (contactName == null ? "" : contactName), true);
	}

	public void callInitiated(int callId) {
		Logger.log(TAG, "Call initiated");
		
		callMessage = new CallMessage();
		//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1
		String number = Phone.getCall(callId).getPhoneNumber();
		//#else
		String number = Phone.getCall(callId).getDisplayPhoneNumber();
		//#endif
		String contactName = new PhoneCallLogID(number).getName();
		callMessage.callStarted(number, (contactName == null ? "" : contactName), true);
	}

	public void callEndedByUser(int callId) {
		Logger.log(TAG, "Call ended by user");

		callMessage.callEnded(CallEndStatus.FINISHED);
		ActivityLog.addMessage(callMessage);
		Logger.log(TAG, "Call message added to log");
	}

	public void callDisconnected(int callId) {
		Logger.log(TAG, "Call disconnected");

		callMessage.callEnded(CallEndStatus.DROPPED);
		ActivityLog.addMessage(callMessage);
		Logger.log(TAG, "Call message added to log");
	}

	/**
	 * Holds call events in Message format.
	 */
	public static class CallMessage {
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
		public String toString() {
			return Registration.getRegID() + Server.separator
					+ type + Server.separator + callTimeStamp
					+ Server.separator + number
					+ Server.separator + contactName
					+ Server.separator + callDuration
					+ Server.separator + outgoing
					+ Server.separator + endStatus;
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
