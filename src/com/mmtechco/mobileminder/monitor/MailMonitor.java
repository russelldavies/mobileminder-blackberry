package com.mmtechco.mobileminder.monitor;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.mail.event.StoreEvent;
import net.rim.blackberry.api.mail.event.StoreListener;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;

public class MailMonitor implements FolderListener, StoreListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(MailMonitor.class);

	private LocalDataWriter actLog;
	MailMessage messageObject;
	Message emailMessage;
	Logger logger = Logger.getInstance();
	boolean _hasSupportedAttachment = false;
	boolean _hasUnsupportedAttachment = false;

	public MailMonitor(LocalDataWriter inputAccess) {
		actLog = inputAccess;
		messageObject = new MailMessage();

		// Recursively search all folders on device and search for Inbox/Outbox.
		// This is more flexible than specifying a path.
		ServiceBook sb = ServiceBook.getSB();
		ServiceRecord[] srs = sb.getRecords();

		for (int cnt = srs.length - 1; cnt >= 0; --cnt) {
			// identify the service record associated with a mail message
			// service via a CID of 'CMIME'
			if (srs[cnt].getCid().equals("CMIME")) {
				ServiceConfiguration sc = new ServiceConfiguration(srs[cnt]);
				Store store = Session.getInstance(sc).getStore();
				// store.addFolderListener(this);

				// then search recursively for INBOX and OUTBOX folders
				Folder[] folders = store.list();
				for (int foldercnt = folders.length - 1; foldercnt >= 0; --foldercnt) {
					Folder f = folders[foldercnt];
					recurse(f);
				}
			}
		}
	}

	/**
	 * Recursive method to search a folder, and its sub-folders on the device to
	 * see if it matches "Folder.INBOX"
	 * 
	 * @param folder
	 *            - the folder which we want to compare with "Folder.INBOX"
	 */
	public void recurse(Folder folder) {
		// Add listener if it matches
		if (folder.getType() == Folder.INBOX || folder.getType() == Folder.SENT) {
			logger.log(TAG,
					"Folder matching INBOX found! " + folder.getFullName());
			folder.addFolderListener(this);
		}
		Folder[] farray = folder.list();
		// Search all the folders sub-folders
		for (int fcnt = farray.length - 1; fcnt >= 0; --fcnt) {
			recurse(farray[fcnt]);
		}
	}

	// Folders and listeners should all be taken care of at this stage
	// Now we get onto handling the incoming message
	public void messagesAdded(FolderEvent e) {
		messageObject.clearData();
		logger.log(TAG, "Email message "
				+ (e.getMessage().isInbound() ? "received" : "sent"));
		emailMessage = e.getMessage();

		boolean isInbound = (e.getMessage().isInbound() ? true : false);
		messageObject.setMailDirection(isInbound);

		try {
			logger.log(TAG, "Setting Message");
			if (isInbound) {
				// If it is inbound it should also only have 1 "from",
				// but maybe other TO or CCs
				String name = emailMessage.getFrom().getName();
				name = name.substring(1, emailMessage.getFrom().getName()
						.length() - 1);

				messageObject.setMessage(emailMessage.getFrom().getAddr(),
						name, emailMessage.getSubject(), emailMessage
								.getBodyText(),
						emailMessage.isInbound() ? (byte) 1 : (byte) 0,
						emailMessage.getSentDate().toString(),
						_hasSupportedAttachment || _hasUnsupportedAttachment);
			} else {
				// Retrieve all types of recipient
				Address[] sentTo = emailMessage
						.getRecipients(Message.RecipientType.TO);
				Address[] sentCc = emailMessage
						.getRecipients(Message.RecipientType.CC);
				Address[] sentBcc = emailMessage
						.getRecipients(Message.RecipientType.BCC);

				// Loops through the arrays and pulls out Recipient names
				// StringBuffer allRecipientsNames = new StringBuffer();
				StringBuffer allRecipientsEmails = new StringBuffer();

				for (int count = 0; count < sentTo.length; count++) {
					allRecipientsEmails.append(sentTo[count].getAddr());
					allRecipientsEmails.append(";");
					// allRecipientsNames.append(sentTo[count].getName().substring(1,
					// sentTo[count].getName().length()-1));
					// allRecipientsNames.append(";"); // This "name" substring
					// is seperated by a ";"
				}
				for (int count = 0; count < sentCc.length; count++) {
					allRecipientsEmails.append(sentCc[count].getAddr());
					allRecipientsEmails.append(";");
					// allRecipientsNames.append(sentCc[count].getName().substring(1,
					// sentCc[count].getName().length()-1));
					// allRecipientsNames.append(";"); // This "name" substring
					// is seperated by a ";"
				}
				for (int count = 0; count < sentBcc.length; count++) {
					allRecipientsEmails.append(sentBcc[count].getAddr());
					allRecipientsEmails.append(";");
					// allRecipientsNames.append(sentBcc[count].getName().substring(1,
					// sentBcc[count].getName().length()-1));
					// allRecipientsNames.append(";"); // This "name" substring
					// is seperated by a ";"
				}

				messageObject.setMessage(
						allRecipientsEmails.toString(),
						"", // No names on outbound emails, just the email
							// address itself
						emailMessage.getSubject(), emailMessage.getBodyText(),
						emailMessage.isInbound() ? (byte) 1 : (byte) 0,
						emailMessage.getSentDate().toString(),
						_hasSupportedAttachment || _hasUnsupportedAttachment);
			}

			logger.log(TAG, "Message set");
			logger.log(TAG, "RESTstring: " + messageObject.getREST());
		} catch (MessagingException e1) {
			logger.log( TAG, e1.getMessage());
			e1.printStackTrace();
		}
		logger.log(TAG, "Adding message to log");
		actLog.addMessage(messageObject);
	}

	public void messagesRemoved(FolderEvent e) {
		logger.log(TAG, "Messages deleted");
	}

	public void batchOperation(StoreEvent e) {
		logger.log(TAG, "Batch operation");
	}
}

class MailDirectionStatus {
	private MailDirectionStatus() {
	}

	public static final byte INBOUND = 0;
	public static final byte OUTBOUND = 1;
}

/**
 * Implements the message interface to hold mail event messages.
 */
class MailMessage implements com.mmtechco.mobileminder.prototypes.Message {
	private final String type = "3"; // 3 = Email message
	private String contactEmail;
	private String contactName;
	private String emailSubject;
	private String emailBody;
	private String deviceTime;
	private byte sentDirection; // 0:Incoming/1:Outgoing
	private boolean hasAttachment;
	private String sentTime;
	private MMTools tools;

	/**
	 * The constructor initialises a normal mail message -> "01"
	 */
	public MailMessage() {
		clearData();
	}

	/**
	 * This method adds the email event information to the mail message object.
	 * 
	 * @param _address
	 *            the email was sent to or received from
	 * @param _outgoing
	 *            states whether the mail was outgoing or incoming
	 */
	public void setMessage(String _email, byte _outgoing) {
		contactEmail = _email;
		sentDirection = _outgoing;
		deviceTime = tools.getDate();
	}

	/**
	 * This method adds the mail event information to the mail message object.
	 * 
	 * @param _email
	 *            the contacts email address
	 * @param _outgoing
	 *            states whether the mail was outgoing or incoming.
	 * @param _deviceTime
	 *            Time when the mail was sent.
	 * @param _hasAttachment
	 *            boolean indicating if the email had an attachment or not
	 */
	public void setMessage(String _email, String _contact,
			String _emailSubject, String _mailBody, byte _outgoing,
			String _sentTime, boolean _hasAttachment) {
		deviceTime = tools.getDate();
		contactEmail = _email;
		contactName = _contact;
		emailSubject = _emailSubject;
		emailBody = _mailBody;
		sentDirection = _outgoing;
		sentTime = _sentTime;
		hasAttachment = _hasAttachment;
	}

	/**
	 * This method removes the current data in the message and initialises the
	 * parameters.
	 * 
	 */
	public void clearData()// This is used to ensure good practices and save
							// resources on the device.
	{
		deviceTime = null;
		contactEmail = null;
		contactName = null;
		hasAttachment = false;
		sentTime = null;
	}

	/**
	 * This method retrieves the message formatted in to a single string value.
	 * <p>
	 * Mail message consists of:
	 * <ul>
	 * <li>Registration Serial number.
	 * <li>Mail message type (two digits number).<br>
	 * '11' is used If call message is synchronised and '01' is used if message
	 * is not synchronised.
	 * <li>Time on the device when the mail was sent.
	 * <li>Contact Email address.
	 * <li>Contact Name.
	 * <li>Sent Time of the mail itself
	 * <li>Mail sent status ( Incoming or outgoing )
	 * <li>Attachment present (true,false)
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator
				+ type + Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + contactEmail
				+ Tools.ServerQueryStringSeparator + contactName
				+ Tools.ServerQueryStringSeparator + emailSubject
				+ Tools.ServerQueryStringSeparator + emailBody
				+ Tools.ServerQueryStringSeparator + sentTime
				+ Tools.ServerQueryStringSeparator + sentDirection
				+ Tools.ServerQueryStringSeparator
				+ String.valueOf((hasAttachment) ? (byte) 1 : (byte) 0);
	}

	/**
	 * This method records the sent time of the email.
	 */
	public void setSentTime(String _sentTime) {
		sentTime = _sentTime;
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
	 * Returns the time at which the message was sent
	 * 
	 * @return The time the email was sent
	 */
	public String getSentTime() {
		return sentTime;
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
	 * Sets the direction of the email, ie inbound (received), or outbound
	 * (sent).
	 * 
	 * @param isInbound
	 *            true for inbound, false for outbound
	 */
	public void setMailDirection(boolean isInbound) {
		sentDirection = (isInbound ? MailDirectionStatus.INBOUND
				: MailDirectionStatus.OUTBOUND);
	}
}