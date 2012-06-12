package com.mmtechco.mobileminder.monitor;

import java.util.Date;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.sms.OutboundMessageListener;

/**
 * 
 * MyTextListener monitors and registers text message based events.
 * 
 */
public class SMSMonitor implements OutboundMessageListener, MessageListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(SMSMonitor.class);

	private static Logger logger = Logger.getInstance();
	private static MessageConnection receiver;
	private MMTools tools = ToolsBB.getInstance();
	private SMSMessage smsMessage;

	/**
	 * The constructor initialises the action store location and registers a
	 * MessageListener for the device.
	 * 
	 * @param inputAccess
	 *            log of actions
	 */
	public SMSMonitor() {
		logger.log(TAG, "Started");
		
		smsMessage = new SMSMessage();

		try {
			receiver = (MessageConnection)Connector.open("sms://:0");
			if (receiver != null) {
				receiver.setMessageListener(this);
			}
		} catch (Exception e) {
			logger.log(TAG, e.getMessage());
		}
	}

	/**
	 * Adds a message and its address to the action log
	 * 
	 * @param inputStatus
	 *            message structure
	 * @param inputDestinationAddress
	 *            message address
	 * @param date
	 * @param txtBody
	 */
	private void addToLog(String inputStatus, String contactNumber,
			String date, String messageBody) {
		logger.log(TAG, "Adding message to log");

		smsMessage.clearData();
		boolean isOutgoing = inputStatus.equalsIgnoreCase("Outgoing Message") ? true
				: false;

		logger.log(TAG, "inputStatus is: " + inputStatus);
		logger.log(TAG, "SMS Address: " + contactNumber);
		logger.log(TAG, "Direction is: "
				+ (isOutgoing ? "Outgoing" : "Incoming"));
		logger.log(TAG, "Date: " + date);
		logger.log(TAG, "SMS Body: " + messageBody);

		smsMessage.setMessage(contactNumber, isOutgoing, date, messageBody);
		// set contact name seperately
		String contactName = new PhoneCallLogID(contactNumber).getName();
		smsMessage.setContactName((null == contactName ? "" : contactName));

		logger.log(TAG, "Adding message to log...");
		ActivityLog.addMessage(smsMessage);
		logger.log(TAG, "Message added to log...");
	}

	public void notifyOutgoingMessage(Message message) {
		logger.log(TAG, "SMS message outgoing");

		String messageBody = "";
		// If "message" is an object of type "TextMessage"
		if (message instanceof TextMessage) {
			messageBody = ((TextMessage) message).getPayloadText();
		}
		String recipient = message.getAddress();
		// Strip off 'sms://'
		recipient = recipient.substring(recipient.lastIndexOf('/')) + 1;
		addToLog("Outgoing Message", recipient, tools.getDate(), messageBody);
	}

	// In the simulator, ensure source and destination SMS ports are the same in
	// the Network menu, this way you will receive the text you just sent, and
	// can test notifyIncomingMessage
	public void notifyIncomingMessage(MessageConnection conn) {
		logger.log(TAG, "SMS message incoming");

		TextMessage message = null;
		try {
			message = (TextMessage) conn.receive();
		} catch (Exception e) {
			logger.log(TAG, "Could not read SMS");
		}
		String sender = message.getAddress();
		// Strip off 'sms://'
		sender = sender.substring(sender.lastIndexOf('/')) + 1;
		addToLog("Incoming Message", sender, tools.getDate(),
				message.getPayloadText());
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
		return Registration.getRegID() + Server.separator
				+ type + Server.separator + deviceTime
				+ Server.separator + number
				+ Server.separator + contactName
				+ Server.separator + startStatus
				+ Server.separator + endStatus
				+ Server.separator + messageBody;
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