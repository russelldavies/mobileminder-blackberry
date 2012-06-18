package com.mmtechco.mobileminder.contacts;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.pim.PIM;

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
import com.mmtechco.mobileminder.prototypes.IMAGE_TYPES;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.CRC32;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Used to retrieve the contact picture stored on the device based on a
 * specified phone number that is sent in a command.
 */
public class ContactPic implements Controllable {
	private static Logger logger = Logger.getLogger(ContactPic.class);
	private MMTools tools = ToolsBB.getInstance();

	/**
	 * This method extracts the contact picture stored on the device
	 * 
	 * @param inputNumber
	 *            - picture to be extracted based on this phone number
	 * @return a ContactPhotoContainer is returned consisting of the contact
	 *         picture, type of image and an email address, if specified in the
	 *         contact
	 */
	public ContactPhotoContainer getContactPhotoFromNumber(String inputNumber) {
		ContactPhotoContainer photoObject = new ContactPhotoContainer();
		try {
			BlackBerryContactList contactList = (BlackBerryContactList) PIM
					.getInstance()
					.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);

			// Get a list of contacts that match the phone number (last 8 digits
			// only)
			Enumeration contacts = contactList.items(
					inputNumber.substring(inputNumber.length() - 8),
					BlackBerryContact.TEL);

			while (contacts.hasMoreElements()) {
				logger.debug("ContactPic::enum not empty");

				BlackBerryContact contact = (BlackBerryContact) contacts
						.nextElement();
				// byte[] byteStream = c.getBinary(BlackBerryContact.PHOTO,
				// BlackBerryContact.ATTR_NONE);
				logger.debug("Counting number of contact photos");
				if (contact.countValues(BlackBerryContact.PHOTO) > 0) {
					logger.debug("ContactPic amount > 0");
					byte[] photoEncoded = contact.getBinary(
							BlackBerryContact.PHOTO, 0);
					logger.debug("Decoding image...");
					byte[] photoDecoded = Base64InputStream.decode(
							photoEncoded, 0, photoEncoded.length);
					logger.debug("Creating pic to upload...");
					EncodedImage contactPic = EncodedImage.createEncodedImage(
							photoDecoded, 0, photoDecoded.length);
					logger.debug("Getting pic type");
					String picType = String.valueOf(contactPic.getImageType());

					logger.debug("Setting photo in ContactPhotoContainer object...");
					photoObject.setPhoto(byteArrayToHexString(photoDecoded));
					logger.debug("Setting photo type in ContactPhotoContainer object...");
					photoObject.setPhotoType(getFileType(picType));
					logger.debug("FileType=:" + photoObject.photoType);
				}
				if (contact.countValues(BlackBerryContact.EMAIL) > 0) {
					logger.debug("Setting email in ContactPhotoContainer object...");
					photoObject.setEmail(contact.getString(
							BlackBerryContact.EMAIL, 0));
					logger.debug("Email=:" + photoObject.email);
				}
				// get the last line of the hex string
				int length = photoObject.photoStream.length();
				logger.debug("Last values = Hex:" + photoObject.photoStream.substring( (length - 100), length) + ":");

				// get hex input stream string split into 100 character pieces
				int len = photoObject.photoStream.length();
				int count = 0, num = 100;
				while (count < len) {
					if (count == num) {
						logger.debug("Hex:" + photoObject.photoStream.substring( (count - 100), count) + ":");
						num += 100;
					}
					count++;
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		return photoObject;
	}

	public String byteArrayToHexString(byte[] b) {
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

	public String getFileType(String picType) {
		int theType = (int) Integer.parseInt(picType);
		String typeToReturn = null;

		switch (theType) {
		case 1:
			typeToReturn = IMAGE_TYPES.GIF;
			break;
		case 2:
			typeToReturn = IMAGE_TYPES.PNG;
			break;
		case 3:
			typeToReturn = IMAGE_TYPES.JPEG;
			break;
		case 4:
			typeToReturn = IMAGE_TYPES.WBMP;
			break;
		case 5:
			typeToReturn = IMAGE_TYPES.BMP;
			break;
		case 6:
			typeToReturn = IMAGE_TYPES.TIFF;
			break;
		case 9:
			typeToReturn = IMAGE_TYPES.ICO;
			break;
		default:
			typeToReturn = IMAGE_TYPES.UNKNOWN;
			break;
		}
		return typeToReturn;
	}

	public boolean processCommand(String[] inputArgs) {
		logger.debug("Processing Contact Photo Command...");

		boolean complete = false;

		// check for valid command message
		if (true == tools.containsOnlyNumbers(inputArgs[2])) {
			logger.debug("args[0] :" + inputArgs[0]);
			if (inputArgs[0].equalsIgnoreCase("pic")) {
				if (inputArgs[1].equalsIgnoreCase("call")) {
					logger.debug("args[1] :" + inputArgs[1]);
					logger.debug("args[2] :" + inputArgs[2]);

					String contactNumber = inputArgs[2];

					// find picture
					ContactPhotoContainer photoPackage = getContactPhotoFromNumber(contactNumber);

					if (null != photoPackage.photoStream) {
						ContactPicMessage message = new ContactPicMessage(
								photoPackage.photoType, contactNumber,
								photoPackage.email);

						logger.debug("ADDING CONTACT PIC DATA");

						// send picture message
						logger.debug("Sending Contact Photo Command...");
						Hashtable keyvalPairs = new Hashtable();
						CRC32 crc = new CRC32();
						crc.update(photoPackage.photoStream.getBytes());
						keyvalPairs.put("crc", String.valueOf(crc.getValue()));
						keyvalPairs.put("pic", photoPackage.photoStream);
						try {
							Response response = Server.post(message.toString(),
									keyvalPairs);
							Reply.Regular reply = new Reply.Regular(
									response.getContent());
							if (!reply.error) {
								complete = true;
							}
						} catch (IOException e) {
							logger.warn("Connection problem: " + e.getMessage());
						} catch (ParseException e) {
							ActivityLog.addMessage(new ErrorMessage(e));
						}
					} else {
						complete = true;
					}
				}
			}
		} else {
			complete = true;
		}

		return complete;
	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.CONTACTS) {
			return true;
		} else {
			return false;
		}
	}

	static class ContactPicMessage extends Message {
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

	/**
	 * Container for contact information to be sent between methods in the
	 * ContactPic class. This container consists of the contact photo, the type
	 * of image and the email address stored in the contact information.
	 */
	class ContactPhotoContainer {
		public String photoStream; // Photo data base64 encoded
		public String photoType;
		public String email;

		public void setPhoto(String photoStream) {
			this.photoStream = photoStream;
		}

		public void setPhotoType(String photoType) {
			this.photoType = photoType;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	}
}