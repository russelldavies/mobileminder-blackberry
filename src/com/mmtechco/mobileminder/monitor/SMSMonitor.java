package com.mmtechco.mobileminder.monitor;

import java.io.IOException;

import java.io.InterruptedIOException;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.sms.OutboundMessageListener;

/**
 * 
 * MyTextListener monitors and registers text message based events.
 * 
 */
public class SMSMonitor implements OutboundMessageListener,
		javax.wireless.messaging.MessageListener, Controllable {
	private static final String TAG = ToolsBB.getSimpleClassName(SMSMonitor.class);

	private LocalDataWriter actLog;
	public static Logger logger = Logger.getInstance();
	MessageConnection _mc;
	private MMTools tools = ToolsBB.getInstance();
	private SMSMessage smsMessage;

	/**
	 * The constructor initialises the action store location and registers a
	 * MessageListener for the device.
	 * 
	 * @param inputAccess
	 *            log of actions
	 */
	public SMSMonitor(LocalDataWriter inputAccess) {
		logger.log(TAG, "Starting MyTextListener...");
		actLog = inputAccess;
		smsMessage = new SMSMessage();

		try {
			_mc = (MessageConnection) Connector.open("sms://:1212"); // is port
																		// 1212
																		// valid??
			_mc.setMessageListener(this);
		} catch (IOException e) {
			logger.log(TAG, "MyTextListener::IOException:: " + e.getMessage());
			// TODO: actLog.addMessage(smsMessage);
		} catch (Exception e) {
			logger.log(TAG, "MyTextListener::Exception:: " + e.getMessage());
			// TODO:actLog.addMessage(smsMessage);
		}
	}

	/**
	 * Adds a message and its address to the action log
	 * 
	 * @param inputStatus
	 *            message structure
	 * @param inputDestinationAddress
	 *            message address
	 * @param _date
	 * @param txtBody
	 */

	private void addToLog(String inputStatus, String contactNumber,
			String _date, String _txtBody) {
		logger.log(TAG, "Adding to log:MyTextListener");
		smsMessage.clearData();
		boolean isOutgoing = inputStatus.equalsIgnoreCase("Outgoing Message") ? true
				: false;

		logger.log(TAG, "inputStatus is: " + inputStatus);
		logger.log(TAG, "SMS Address: " + contactNumber);
		logger.log(TAG, "Direction is: "
				+ (isOutgoing ? "Outgoing" : "Incoming"));
		logger.log(TAG, "Date: " + _date);
		logger.log(TAG, "SMS Body: " + _txtBody);

		smsMessage.setMessage(contactNumber, isOutgoing, _date, _txtBody);
		// set contact name seperately
		String contactName = new PhoneCallLogID(contactNumber).getName();
		smsMessage.setContactName((null == contactName ? "" : contactName));

		logger.log(TAG, "Adding message to log...");
		actLog.addMessage(smsMessage);
		logger.log(TAG, "Message added to log...");
	}

	/**
	 * Adds an outgoing message to the action log
	 * 
	 * @param message
	 *            message structure
	 */

	public void notifyOutgoingMessage(Message message) {
		logger.log(TAG, "SMS message outgoing");

		// Get the body contents of the SMS Text
		String txtBody = "";
		// If "message" is an object of type "TextMessage"
		if (message instanceof TextMessage) {
			txtBody = ((TextMessage) message).getPayloadText();
		}

		// Add to log, but get a proper formatted PH Number without sms:// at
		// the start
		String phNumber = message.getAddress();
		addToLog("Outgoing Message",
				phNumber.substring(phNumber.lastIndexOf('/') + 1),
				tools.getDate(), txtBody);
	}

	/**
	 * Adds an incoming message to the action log
	 * 
	 * @param message
	 *            message structure
	 */

	// In the simulator, ensure source and destination SMS ports are the same in
	// the Network menu,
	// This way, you will receive the text you just sent, and can test
	// notifyIncomingMessage
	public void notifyIncomingMessage(MessageConnection conn) {
		logger.log(TAG, "SMS message incoming");

		// Get the message
		TextMessage message = null;
		try {
			message = (TextMessage) conn.receive();
		} catch (InterruptedIOException e1) {
			logger.log(TAG,
					"MyTextListener::notifyIncomingMessage::InterruptedIOException::"
							+ e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.log(
					TAG,
					"MyTextListener::notifyIncomingMessage::IOException::"
							+ e1.getMessage());
			e1.printStackTrace();
		}

		String phNumber = message.getAddress();
		addToLog("Incoming Message",
				phNumber.substring(phNumber.lastIndexOf('/')), tools.getDate(),
				message.getPayloadText());

		// http://myhowto.org/java/j2me/22-sending-and-receiving-gsm-sms-on-blackberry-using-rim-apis/
		// http://www.blackberry.com/developers/docs/6.0.0api/Messaging-summary.html#MG_3
	}

	public boolean isTarget(COMMAND_TARGETS inputCOMMAND_TARGETS) {
		logger.log(TAG, "TextListener::isTarget::COMMAND_TARGETS.");
		if (inputCOMMAND_TARGETS == COMMAND_TARGETS.TEXT) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isTarget(String inputCOMMAND_TARGETS) {
		logger.log(TAG, "TextListener::isTarget::String.");
		if (inputCOMMAND_TARGETS == COMMAND_TARGETS.TEXT.toString()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean processCommand(String[] arg0) {
		logger.log(TAG, "TextListener::processCommand::String[].");
		// TODO Auto-generated method stub
		return false;
	}
}


/**
 * Implements the message interface to hold SMS event messages.
 */
class SMSMessage implements com.mmtechco.mobileminder.prototypes.Message {
	private final String type;
	private String number;
	private String contactName;
	private String messageBody;
	private String deviceTime;
	private boolean startStatus;// 0:Incoming/1:Outgoing
	private boolean endStatus; // 0:bounced /1:delivered

	public SMSMessage() {
		this(false);
	}

	public SMSMessage(boolean sync) {
		if (sync) {
			type = "12";
		} else {
			type = "02";
		}
		clearData();
	}

	/**
	 * Adds the SMS event information to the SMS message object
	 * 
	 * @param _number
	 *            the phone number of the incoming or outgoing message
	 * @param _outgoing
	 *            states whether the SMS was outgoing or incoming
	 * @param _date
	 * @param _inputBody
	 *            The body of the SMS message
	 */
	public void setMessage(String _number, boolean _outgoing, Date _date,
			String _inputBody) {
		setMessage(_number, _outgoing, _date, _inputBody);
	}

	/**
	 * Adds the SMS event information to the SMS message object
	 * 
	 * @param _number
	 *            the phone number of the incoming or outgoing message
	 * @param _outgoing
	 *            states whether the SMS was outgoing or incoming
	 * @param _deviceTime
	 *            Time when the SMS is being made.
	 * @param inputBody
	 *            The body of the SMS message
	 */
	public void setMessage(String _number, boolean _outgoing,
			String _deviceTime, String inputBody) {
		clearData();
		startStatus = _outgoing;// 0:Incoming/1:Outgoing
		number = _number;
		deviceTime = _deviceTime;
		messageBody = inputBody;
	}

	/**
	 * This method removes the current data in the message and initialises the
	 * parameters.
	 */
	public void clearData() {
		startStatus = false;// Incoming
		endStatus = true; // delivered
		messageBody = "";
		contactName = "";
	}

	/**
	 * This method retrieves the information in the body of the message
	 * 
	 * @return the message body
	 */
	public String getMessageBody() {
		return messageBody;
	}

	/**
	 * This method retrieves the message formatted in to a single string value.
	 * <p>
	 * SMS message consists of:
	 * <ul>
	 * <li>Registration Serial number.
	 * <li>Type of SMS message.
	 * <li>Device time.
	 * <li>Phone number
	 * <li>Status ( Incoming or outgoing ).
	 * <li>Status ( Delivered or Bounced )
	 * <li>The body of the SMS message.
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + type
				+ Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + number
				+ Tools.ServerQueryStringSeparator + contactName
				+ Tools.ServerQueryStringSeparator + startStatus
				+ Tools.ServerQueryStringSeparator + endStatus
				+ Tools.ServerQueryStringSeparator + messageBody;
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
	 * This method retrieves the type number for the SMS message
	 * 
	 * @return the type number corresponding to a SMS message
	 */
	public int getType() {
		return Integer.parseInt(type);
	}

	public void setContactName(String theName) {
		contactName = theName;
	}

}