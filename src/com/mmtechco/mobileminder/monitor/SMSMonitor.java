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
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

public class SMSMonitor implements MessageListener, OutboundMessageListener {
	private static Logger logger = Logger.getLogger(SMSMonitor.class);
	private static MMTools tools = ToolsBB.getInstance();

	private static MessageConnection receiver;

	public SMSMonitor() {
		try {
			receiver = (MessageConnection) Connector.open("sms://:0");
			if (receiver != null) {
				receiver.setMessageListener(this);
			}
		} catch (IOException e) {
			ActivityLog.addMessage(new ErrorMessage("Could not listen for SMS",
					e));
		}
		logger.info("Started listening");
	}

	// In the simulator, ensure source and destination SMS ports are the same in
	// the Network menu, this way you will receive the text you just sent, and
	// can test notifyIncomingMessage
	public void notifyIncomingMessage(MessageConnection conn) {
		logger.debug("Incoming message");

		try {
			TextMessage message = (TextMessage) conn.receive();
			String sender = message.getAddress();
			// Strip off 'sms://'
			sender = sender.substring(sender.lastIndexOf('/')) + 1;
			addToLog(false, sender,
					tools.getDate(message.getTimestamp().getTime()),
					message.getPayloadText());
		} catch (IOException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		}
	}

	public void notifyOutgoingMessage(Message message) {
		logger.debug("SMS message outgoing");

		if (message instanceof TextMessage) {
			String recipient = message.getAddress();
			// Strip off 'sms://'
			recipient = recipient.substring(recipient.lastIndexOf('/')) + 1;
			String messageBody = ((TextMessage) message).getPayloadText();
			addToLog(true, recipient,
					tools.getDate(message.getTimestamp().getTime()),
					messageBody);
		}
	}

	private void addToLog(boolean outgoing, String contactNumber, String date, String messageBody) {
		logger.debug("Adding message to log");

		// Get contact name if exists
		String contactName = new PhoneCallLogID(contactNumber).getName();
		contactName = (null == contactName) ? "Unknown" : contactName;
		
		boolean delivered = true; // No way to check delivery status
		ActivityLog.addMessage(new SMSMessage(date, contactNumber, contactName, outgoing, delivered, messageBody));
	}
}

class SMSMessage extends com.mmtechco.mobileminder.net.Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>Device time
	 * <li>Number of SMS recipient or sender
	 * <li>Contact Name
	 * <li>Direction: 0 = incoming, 1 = outgoing
	 * <li>Delivery: 0 = bounced, 1 = delivered
	 * <li>SMS content
	 * </ul>
	 */
	// public SMSMessage(String number, String name, boolean outgoing, boolean
	// delivered, String time, String content) {
	public SMSMessage(String time, String number, String name,
			boolean outgoing, boolean delivered, String content) {
		super(com.mmtechco.mobileminder.net.Message.SMS, new String[] {
				ToolsBB.getInstance().getDate(), number, name,
				(outgoing ? "1" : "0"), (delivered ? "1" : "0"), content });
	}
}