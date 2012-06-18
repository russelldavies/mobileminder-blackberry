package com.mmtechco.mobileminder.monitor;

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

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class MailMonitor implements FolderListener, StoreListener {
	private static Logger logger = Logger.getLogger(MailMonitor.class);
	
	boolean _hasSupportedAttachment = false;
	boolean _hasUnsupportedAttachment = false;

	public MailMonitor() {
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
			logger.debug("Folder matching INBOX found! " + folder.getFullName());
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
		logger.debug("Email message "
				+ (e.getMessage().isInbound() ? "received" : "sent"));
		Message emailMessage = e.getMessage();

		boolean isInbound = (e.getMessage().isInbound() ? true : false);

		MailMessage message = null;
		try {
			logger.debug("Setting Message");
			if (isInbound) {
				// If it is inbound it should also only have 1 "from",
				// but maybe other TO or CCs
				String name = emailMessage.getFrom().getName();
				name = name.substring(1, emailMessage.getFrom().getName()
						.length() - 1);

				message = new MailMessage(emailMessage.getFrom().getAddr(),
						name, emailMessage.getSubject(), emailMessage
								.getBodyText(),
						emailMessage.isInbound(),
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
				message = new MailMessage("", allRecipientsEmails.toString(),
						emailMessage.getSubject(), emailMessage.getBodyText(),
						emailMessage.isInbound(), emailMessage.getSentDate().toString(), 
						_hasSupportedAttachment);
			}
		} catch (MessagingException e1) {
			ActivityLog.addMessage(new ErrorMessage(e1));
		}
		logger.debug("Adding message to log");
		ActivityLog.addMessage(message);
	}

	public void messagesRemoved(FolderEvent e) {
		logger.debug("Messages deleted");
	}

	public void batchOperation(StoreEvent e) {
		logger.debug("Batch operation");
	}
}

class MailMessage extends com.mmtechco.mobileminder.net.Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>Device time
	 * <li>Contact email address
	 * <li>Contact name
	 * <li>Email timestamp
	 * <li>Direction: 0 = Incoming, 1 = Outgoing
	 * <li>Attachment: 0 = false, 1 = true
	 * </ul>
	 */
	public MailMessage(String email, String contact, String subject,
			String body, boolean outgoing, String timestamp, boolean attachment) {
		super(com.mmtechco.mobileminder.net.Message.CALL, new String[] {
				ToolsBB.getInstance().getDate(),
				email,
				contact,
				timestamp,
				(outgoing ? "1" : "0"),
				(attachment ? "1" : "0")
		});
	}
}