package com.mmtechco.mobileminder.monitor;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.sms.OutboundMessageListener;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * 
 * MyTextListener monitors and registers text message based events.
 * 
 */
public class SMSMonitor implements OutboundMessageListener, MessageListener {
	private static Logger logger = Logger.getLogger(SMSMonitor.class);
	
	private static MessageConnection receiver;
	private MMTools tools = ToolsBB.getInstance();

	/**
	 * The constructor initialises the action store location and registers a
	 * MessageListener for the device.
	 * 
	 * @param inputAccess
	 *            log of actions
	 */
	public SMSMonitor() {
		logger.debug("Started");
		
		try {
			receiver = (MessageConnection)Connector.open("sms://:0");
			if (receiver != null) {
				receiver.setMessageListener(this);
			}
		} catch (IOException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
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
		logger.debug("Adding message to log");

		boolean isOutgoing = inputStatus.equalsIgnoreCase("Outgoing Message") ? true
				: false;
		boolean delivered = false;

		logger.debug("inputStatus is: " + inputStatus);
		logger.debug("SMS Address: " + contactNumber);
		logger.debug("Direction is: "
				+ (isOutgoing ? "Outgoing" : "Incoming"));
		logger.debug("Date: " + date);
		logger.debug("SMS Body: " + messageBody);

		// set contact name seperately
		String contactName = new PhoneCallLogID(contactNumber).getName();
		contactName= (null == contactName) ? "" : contactName;
		

		logger.debug("Adding message to log...");
		ActivityLog.addMessage(new SMSMessage(contactNumber, contactName,
				isOutgoing, delivered, date, messageBody));
		logger.debug("Message added to log...");
	}

	public void notifyOutgoingMessage(Message message) {
		logger.debug("SMS message outgoing");

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
		logger.debug("SMS message incoming");

		TextMessage message = null;
		try {
			message = (TextMessage) conn.receive();
		} catch (Exception e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		}
		String sender = message.getAddress();
		// Strip off 'sms://'
		sender = sender.substring(sender.lastIndexOf('/')) + 1;
		addToLog("Incoming Message", sender, tools.getDate(),
				message.getPayloadText());
	}
}

class SMSMessage extends com.mmtechco.mobileminder.net.Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>Device time
	 * <li>Number of SMS recipient or sender
	 * <li>Contact Name
	 * <li>Start status: 0 = incoming, 1 = outgoing
	 * <li>End status: 0 = bounced, 1 = delivered
	 * <li>SMS content
	 * </ul>
	 */
	public SMSMessage(String number, String name, boolean outgoing,
			boolean delivered, String time, String content) {
		super(com.mmtechco.mobileminder.net.Message.SMS, new String[] {
				number,
				name,
				(outgoing ? "1" : "0"),
				(delivered ? "1" : "0"),
				content
		});
	}
}