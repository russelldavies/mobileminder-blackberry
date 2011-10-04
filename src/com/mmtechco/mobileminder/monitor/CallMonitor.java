package com.mmtechco.mobileminder.monitor;

import java.util.Date;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

/**
 * 
 * MyCallListener monitors and registers call based events.
 * 
 */

public class CallMonitor extends AbstractPhoneListener implements Controllable {
	private static final String TAG = "CallMonitor";

	private LocalDataWriter actLog;
	// private final String Connected = "Connected";
	// private final String Hold_ON = "Hold_ON";
	// private final String Hold_OFF = "Hold_OFF";
	// private final String Dial_OUT = "Dial_OUT";
	// private final String Dial_IN = "Dial_IN";
	// private final String Dropped = "Dropped";
	private final String Finished = "Finished";
	private String Prefix = "";
	private String contactName = "";
	private String contactNumber = "";
	private int callStartTime = 0;
	private int callEndTime = 0;
	private boolean isOutgoing = false;
	Logger logWriter = Logger.getInstance();
	private MMTools tools = ToolsBB.getInstance();
	private CallMessage callMessage;

	/**
	 * The constructor initialises the action store location and registers the
	 * callListener for the device.
	 * 
	 * @param inputAccess
	 *            log of actions
	 */

	public CallMonitor(LocalDataWriter inputAccess) {
		logWriter.log(TAG, "Start MyCallListener");
		callMessage = new CallMessage();
		actLog = inputAccess;
		Phone.addPhoneListener(this);
	}

	/**
	 * Stores call information to action log.
	 * 
	 * <p>
	 * Retrieves the call information from the device and stores it in the
	 * action log as type Call
	 * 
	 * @param ehandler
	 *            String consisting of a prefix and the status of the call i.e.
	 *            connected
	 * 
	 * @param callid
	 *            The address of the caller
	 * 
	 * 
	 */
	private void addToLog(String ehandler, int callId) {
		logWriter.log(TAG, "In MyCallListener.addToLog");

		callMessage.clearData();
		logWriter.log(TAG, "Setting CallMessage...");
		callMessage.setMessage(contactNumber, isOutgoing, tools.getDate(),
				(callEndTime - callStartTime) / 1000);

		callMessage.setContactName((null == contactName ? "" : contactName));

		logWriter.log(TAG, "Adding message to log...");
		actLog.addMessage(callMessage);
		logWriter.log(TAG, "Message added to log...");
	}

	/**
	 * Adds action to log when the call has been connected
	 * 
	 * @param callId
	 *            call address
	 */
	public void callConnected(int callId) {
		logWriter.log(TAG, "MyCallListener::callConnected");
		callStartTime = (int) new Date().getTime();

		PhoneCall callInfo = Phone.getCall(callId);
		contactNumber = callInfo.getPhoneNumber();

		// contactName =
		// PhoneCallLogID(Integer.parseInt(contactNumber)).getName();
		contactName = new PhoneCallLogID(contactNumber).getName();

		// contactName=callInfo.getDisplayPhoneNumber();

		logWriter.log(TAG, "contactName=" + contactName);

		if (null == contactName)
			contactName = "";
	}

	/**
	 * Adds action to log when the call has been directly connected
	 * 
	 * @param callId
	 *            call address
	 */
	public void callDirectConnectConnected(int callId) {
		logWriter.log(TAG, "MyCallListener::callDirectConnectConnected");
		// addToLog(Prefix+Connected, callId);
	}

	/**
	 * Adds action to log when the call has been directly disconnected
	 * 
	 * @param callId
	 *            call address
	 */
	public void callDirectConnectDisconnected(int callId) {
		logWriter.log(TAG, "MyCallListener::callDirectConnectDisconnected");
		if (false == Prefix.equals("")) {
			// addToLog(Prefix+Finished, callId);
			Prefix = "";
		}
	}

	/**
	 * Adds action to log when the call has been disconnected
	 * 
	 * @param callId
	 *            call address
	 */
	public void callDisconnected(int callId) {
		callEndTime = (int) new Date().getTime();
		logWriter.log(TAG, "MyCallListener::callDisconnected");
		if (false == Prefix.equals("")) {
			addToLog(Prefix + Finished, callId);
			Prefix = "";
		}
	}

	/**
	 * Adds action to log when the call has been put on hold
	 * 
	 * @param callId
	 *            call address
	 */
	public void callHeld(int callId) {
		logWriter.log(TAG, "MyCallListener::callHeld");
		// addToLog(Prefix+Hold_ON, callId);
	}

	/**
	 * Adds action to log when the call has arrived
	 * 
	 * @param callId
	 *            call address
	 */
	public void callIncoming(int callId) {
		logWriter.log(TAG, "MyCallListener::callIncoming");
		isOutgoing = false;
		Prefix = "Incoming ";
		// addToLog(Dial_IN, callId);
	}

	/**
	 * Adds action to log when the call has been initiated by the handheld
	 * 
	 * @param callId
	 *            call address
	 */
	public void callInitiated(int callId) {
		logWriter.log(TAG, "MyCallListener::callInitiated");
		isOutgoing = true;
		Prefix = "Outgoing ";
		// addToLog(Dial_OUT, callId);
	}

	/**
	 * Adds action to log when the call has been taken off hold
	 * 
	 * @param callId
	 *            call address
	 */
	// Call taken off of "hold"
	public void callResumed(int callId) {
		logWriter.log(TAG, "MyCallListener::callResumed");
		// addToLog(Prefix+Hold_OFF, callId);
	}

	/**
	 * Adds action to log when the call has failed
	 * 
	 * @param callId
	 *            call address
	 * @param reason
	 *            failure reason
	 */
	public void callFailed(int callId, int reason) {
		logWriter.log(TAG, "MyCallListener::callFailed");
		// addToLog(Prefix+"Dropped", callId);
		Prefix = "";
	}

	public boolean isTarget(COMMAND_TARGETS inputCOMMAND_TARGETS) {
		logWriter.log(TAG, "CallListener::isTarget::COMMAND_TARGETS");
		if (inputCOMMAND_TARGETS == COMMAND_TARGETS.CALL) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isTarget(String inputCOMMAND_TARGETS) {
		logWriter.log(TAG, "CallListener::isTarget::String");
		if (inputCOMMAND_TARGETS.toString() == COMMAND_TARGETS.CALL.toString()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean processCommand(String[] arg0) {
		logWriter.log(TAG, "CallListener::processCommand::String[]");
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This class implements the message interface to hold call event messages.
	 */
	public class CallMessage implements Message {
		private final String type;
		private Date startTime;
		private String deviceTime;
		private String contactName;
		private String number;
		private boolean startStatus;// 0:Incoming/1:Outgoing
		private byte endStatus;
		private int duration;// in Sec
		private MMTools tools;

		/**
		 * The constructor initialises a normal call message
		 */
		public CallMessage() {
			this(false);
		}

		/**
		 * The constructor initialises a normal call message or a sync call
		 * message based on the boolean value passed to it.
		 * 
		 * @param sync
		 *            true if sync message
		 */
		public CallMessage(boolean sync) {
			if (sync) {
				type = "11";
			} else {
				type = "01";
			}
			clearData();
		}

		/**
		 * This method adds the call event information to the call message
		 * object.
		 * 
		 * @param _number
		 *            the phone number received or dialled
		 * @param _outgoing
		 *            states whether the call was outgoing or incoming
		 */
		public void setMessage(String _number, boolean _outgoing) {
			number = _number;
			startStatus = _outgoing;
			deviceTime = tools.getDate();
		}

		/**
		 * This method adds the call event information to the call message
		 * object.
		 * 
		 * @param _number
		 *            the phone number received or dialled.
		 * @param _outgoing
		 *            states whether the call was outgoing or incoming.
		 * @param _deviceTime
		 *            Time when the call begins.
		 * @param _duration
		 *            the length of the call.
		 */
		public void setMessage(String _number, boolean _outgoing,
				String _deviceTime, int _duration) {
			number = _number;
			startStatus = _outgoing;
			deviceTime = _deviceTime;
			duration = _duration;
			setEndStatus();
		}

		public void clearData() {
			deviceTime = "";
			number = "";
			duration = 0;
			startStatus = false;// Incoming
			endStatus = (byte) CallEndStatus.OTHER;// .ordinal();
			startTime = null;
			contactName = "";
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
					+ type + Tools.ServerQueryStringSeparator + deviceTime
					+ Tools.ServerQueryStringSeparator + number
					+ Tools.ServerQueryStringSeparator + contactName
					+ Tools.ServerQueryStringSeparator + duration
					+ Tools.ServerQueryStringSeparator + startStatus
					+ Tools.ServerQueryStringSeparator + endStatus;
		}

		/**
		 * This method records the start time of the call.
		 */
		public void setStartTime() {
			startTime = new Date();
		}

		/**
		 * This method records the contacts name from the phone call.
		 */
		public void setContactName(String name) {
			contactName = name;
		}

		/**
		 * This method retrieves the time that is set on the device.
		 * 
		 * @return the device time
		 */
		public String getTime() {
			return deviceTime;
		}

		/**
		 * This method retrieves the type number for the call message
		 * 
		 * @return the type number corresponding to a call message
		 */
		public int getType() {
			return Integer.parseInt(type);
		}

		/**
		 * This method calculates and records the duration of the call
		 */
		public void setEndDuration() {
			duration = (int) (new Date().getTime() - startTime.getTime()) / 1000;
		}

		/**
		 * This method is used to record "finished" as the way in which the call
		 * was ended.
		 */
		public void setEndStatus() {
			setEndStatus(CallEndStatus.FINISHED);
		}

		/**
		 * This method records the status in which the call was ended.
		 * 
		 * @param _status
		 *            the status in which the call was ended
		 */
		public void setEndStatus(byte _status/* CallEndStatus _status */) {
			endStatus = _status;/* (byte)_status.ordinal(); */
		}
	}
}

/**
 * This is an enumeration of the different ways in which a call can be ended.
 * Since enums don't exist in the blackberry API, a workaround has been
 * implemented
 */
class CallEndStatus {
	private CallEndStatus() {
	}

	// TODO: Implement these properly. ATM, we only set FINISH
	public static final byte OTHER = 0;
	public static final byte NO_ANSWER = 1;
	public static final byte DROPPED = 2;
	public static final byte FINISHED = 3;
}
