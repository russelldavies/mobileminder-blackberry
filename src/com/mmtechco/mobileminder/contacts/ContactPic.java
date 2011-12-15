package com.mmtechco.mobileminder.contacts;

import java.util.Enumeration;
import javax.microedition.pim.PIM;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.LogDb;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.enums.IMAGE_TYPES;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryContactList;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.system.EncodedImage;

/**
 * Used to retrieve the contact picture stored on the device based on a
 * specified phone number that is sent in a command.
 */
public class ContactPic implements Controllable {
	public static final String TAG = ToolsBB.getSimpleClassName(ContactPic.class);

	private Server server;
	private MMTools tools = ToolsBB.getInstance();
	private ContactPicMessage flyContactPicMessage;
	static Logger logger = Logger.getInstance();

	public ContactPic(LogDb actLog) {
		server = new Server(actLog);
		flyContactPicMessage = new ContactPicMessage();
	}

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
				logger.log(TAG, "ContactPic::enum not empty");

				BlackBerryContact contact = (BlackBerryContact) contacts
						.nextElement();
				// byte[] byteStream = c.getBinary(BlackBerryContact.PHOTO,
				// BlackBerryContact.ATTR_NONE);
				logger.log(TAG, "Counting number of contact photos");
				if (contact.countValues(BlackBerryContact.PHOTO) > 0) {
					logger.log(TAG, "ContactPic amount > 0");
					byte[] photoEncoded = contact.getBinary(
							BlackBerryContact.PHOTO, 0);
					logger.log(TAG, "Decoding image...");
					byte[] photoDecoded = Base64InputStream.decode(
							photoEncoded, 0, photoEncoded.length);
					logger.log(TAG, "Creating pic to upload...");
					EncodedImage contactPic = EncodedImage.createEncodedImage(
							photoDecoded, 0, photoDecoded.length);
					logger.log(TAG, "Getting pic type");
					String picType = String.valueOf(contactPic.getImageType());

					logger.log(TAG,
							"Setting photo in ContactPhotoContainer object...");
					photoObject.setPhoto(byteArrayToHexString(photoDecoded));
					logger.log(TAG,
							"Setting photo type in ContactPhotoContainer object...");
					photoObject.setPhotoType(getFileType(picType));
					logger.log(TAG, "FileType=:" + photoObject.photoType);
				}
				if (contact.countValues(BlackBerryContact.EMAIL) > 0) {
					logger.log(TAG,
							"Setting email in ContactPhotoContainer object...");
					photoObject.setEmail(contact.getString(
							BlackBerryContact.EMAIL, 0));
					logger.log(TAG, "Email=:" + photoObject.email);
				}
				// get the last line of the hex string
				int length = photoObject.photoStream.length();
				logger.log(
						TAG,
						"Last values = Hex:"
								+ photoObject.photoStream.substring(
										(length - 100), length) + ":");

				// get hex input stream string split into 100 character pieces
				int len = photoObject.photoStream.length();
				int count = 0, num = 100;
				while (count < len) {
					if (count == num) {
						logger.log(
								TAG,
								"Hex:"
										+ photoObject.photoStream.substring(
												(count - 100), count) + ":");
						num += 100;
					}
					count++;
				}
			}
		} catch (Exception e) {
			logger.log(TAG, e.getMessage());
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

	/**
	 * This method has been overridden from the Controllable interface. By
	 * implementing this interface this class can receive command arguments.
	 * These commands are then processed within this method.
	 * 
	 * @param inputArgs
	 *            arguments sent specifying the command instructions from the
	 *            server.
	 * @return true if the command has been processed with out any errors.
	 */
	public boolean processCommand(String[] inputArgs) {
		logger.log(TAG, "Processing Contact Photo Command...");

		Reply resultREST;
		boolean complete = false;

		// check for valid command message
		if (true == tools.containsOnlyNumbers(inputArgs[2])) {
			logger.log(TAG, "args[0] :" + inputArgs[0]);
			if (inputArgs[0].equalsIgnoreCase("pic")) {
				if (inputArgs[1].equalsIgnoreCase("call")) {
					logger.log(TAG, "args[1] :" + inputArgs[1]);
					logger.log(TAG, "args[2] :" + inputArgs[2]);

					String contactNumber = inputArgs[2];

					// find picture
					ContactPhotoContainer photoPackage = getContactPhotoFromNumber(contactNumber);

					if (null != photoPackage.photoStream) {
						flyContactPicMessage.setMessage(photoPackage.photoType,
								contactNumber, photoPackage.email);

						logger.log(TAG, "ADDING CONTACT PIC DATA");

						// send picture message
						logger.log(TAG, "Sending Contact Photo Command...");
						resultREST = server.contactServer(flyContactPicMessage
								.getREST(), String.valueOf(server
								.getCrcValue(photoPackage.photoStream)),
								photoPackage.photoStream);

						if (resultREST.isError() == false) {
							complete = true;
						}
						flyContactPicMessage.clearData();
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

	/**
	 * This class implements the message interface to hold ContactPic messages.
	 */
	class ContactPicMessage implements Message {
		private final int type = 28;
		private String fileType;
		private String contactNumber;
		private String contactEmail;

		public ContactPicMessage() {
			clearData();
		}

		/**
		 * This method sets the parameters for the ContactPic message to be sent
		 * 
		 * @param inputFileType
		 *            file type of the picture
		 * @param inputContactNumber
		 *            number of the contact
		 * @param inputContactEmail
		 *            email address stored in the contact information
		 */
		public void setMessage(String inputFileType, String inputContactNumber,
				String inputContactEmail) {
			fileType = inputFileType;
			contactNumber = inputContactNumber;
			contactEmail = inputContactEmail;
		}

		public void clearData() {
			fileType = null;
			contactNumber = null;
			contactEmail = null;
		}

		public int getType() {
			return type;
		}

		public String getTime() {
			return "?";
		}

		/**
		 * This method retrieves the ContactPic message formatted into a single
		 * string value. Web message consists of:
		 * <ul>
		 * <li>Registration Serial number.
		 * <li>ContactPic message type which is '28' (two digits number).
		 * <li>Contact picture file type. (e.g; JPEG, PNG, BMP etc )
		 * <li>Contact Name.
		 * <li>Contact Number.
		 * <li>Contact Email.
		 * </ul>
		 * 
		 * @return a single string containing the ContactPic entire message.
		 */
		public String getREST() {
			return Registration.getRegID() + Tools.ServerQueryStringSeparator
					+ type + Tools.ServerQueryStringSeparator + tools.getDate()
					+ Tools.ServerQueryStringSeparator + fileType
					+ Tools.ServerQueryStringSeparator
					+ getContactNameFromNumber(contactNumber)
					+ Tools.ServerQueryStringSeparator + contactNumber
					+ Tools.ServerQueryStringSeparator + contactEmail;
		}

		private String getContactNameFromNumber(String _contactNum) {
			String thePhoneNumber = null;
			PhoneCallLogID callLog = new PhoneCallLogID(_contactNum);
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
		// TODO: replace with file not hex string
		public String photoStream; // This is the photo itself, stored in HEX
									// characters
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