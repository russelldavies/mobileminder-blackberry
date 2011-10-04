package com.mmtechco.mobileminder.contacts;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.UnsupportedFieldException;

import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.enums.IMAGE_TYPES;
import com.mmtechco.util.Logger;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.pdap.BlackBerryContactList;
import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.system.EncodedImage;

/**
 * This class can be used to retrieve the contact picture stored on the device
 * based on a specified phone number that is sent in a command.
 */
public class ContactPic implements Controllable {
	public static final String TAG = "ContactPic";

	private LocalDataWriter actLog;
	private Server myServer;
	private ContactPicMessage flyContactPicMessage;
	static Logger logger = Logger.getInstance();

	/**
	 * The constructor 1. Passes the application context to the instance 2.
	 * creates a server object 3. Initialises a fly message to temporarily hold
	 * ContactPic messages
	 * 
	 * @param inputLocalData
	 *            Storage location for messages
	 */

	public ContactPic(LocalDataWriter inputLocalData) {
		logger.log(TAG, "ContactPic::constructor");
		actLog = inputLocalData;
		myServer = new Server(actLog);
		flyContactPicMessage = new ContactPicMessage();
	}

	/**
	 * This method extracts the contact picture stored on the device
	 * 
	 * @param inputNumber
	 *            picture to be extracted based on this phone number
	 * @return a ContactPhotoContainer is return consisting of the contact
	 *         picture, type of image and an email address, if specified in the
	 *         contact
	 */
	public ContactPhotoContainer getContactPhotoFromNumber(String inputNumber) {
		logger.log(TAG, "In-> getContactPhotoFromNumber");
		ContactPhotoContainer photoObject = new ContactPhotoContainer();

		try {
			BlackBerryContactList contactList = (BlackBerryContactList) PIM
					.getInstance()
					.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);

			// Get a list of contacts that match the phone number (last 8 digits
			// only)
			Enumeration _enum = contactList.items(
					inputNumber.substring(inputNumber.length() - 8),
					BlackBerryContact.TEL);

			logger.log(TAG, "ContactPic::Checking if enum is empty...");
			while (_enum.hasMoreElements()) {
				logger.log(TAG, "ContactPic::enum not empty");

				BlackBerryContact c = (BlackBerryContact) _enum.nextElement();
				// byte[] byteStream = c.getBinary(BlackBerryContact.PHOTO,
				// BlackBerryContact.ATTR_NONE);
				logger.log(TAG, "ContactPic::Counting num of Contact.PHOTOs...");
				if (c.countValues(BlackBerryContact.PHOTO) > 0) {
					logger.log(TAG, "ContactPic amount > 0");
					byte[] photoEncoded = c.getBinary(BlackBerryContact.PHOTO,
							0);
					logger.log(TAG, "ContactPic::Decoding image...");
					byte[] photoDecoded = Base64InputStream.decode(
							photoEncoded, 0, photoEncoded.length);

					logger.log(TAG, "ContactPic::Creating pic to upload...");
					EncodedImage contactPic = EncodedImage.createEncodedImage(
							photoDecoded, 0, photoDecoded.length);
					logger.log(TAG,
							"ContactPic::Getting pic type, ie JPG, PNG etc...");
					String picType = String.valueOf(contactPic.getImageType());

					logger.log(TAG,
							"ContactPic::Setting photo in ContactPhotoContainer object...");
					photoObject.setPhoto(byteArrayToHexString(photoDecoded));
					logger.log(TAG,
							"ContactPic::Setting photo type in ContactPhotoContainer object...");
					photoObject.setPhotoType(getFileType(picType));// get+set
																	// the file
																	// type
					logger.log(TAG, "FileType=:" + photoObject.photoType);
				}
				if (c.countValues(BlackBerryContact.EMAIL) > 0) {
					logger.log(TAG,
							"ContactPic::Setting email in ContactPhotoContainer object...");
					photoObject.setEmail(c
							.getString(BlackBerryContact.EMAIL, 0));
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
								"Contact Photo = Hex:"
										+ photoObject.photoStream.substring(
												(count - 100), count) + ":");
						num += 100;
					}
					count++;
				}
			}
			logger.log(TAG, "ContactPic::After the while to check enum");
		} catch (PIMException e) {
			logger.log(TAG,
					"x::ContactPic::getContactPhotoFromPhoneNumber::PIMException::"
							+ e.getMessage());
			e.printStackTrace();
		} catch (UnsupportedFieldException e) {
			logger.log(TAG,
					"x::ContactPic::getContactPhotoFromNumber::UnsupportedFieldException::"
							+ e.getMessage());
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			logger.log(TAG,
					"x::ContactPic::getContactPhotoFromNumber::IndexOutOfBoundsException::"
							+ e.getMessage());
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			logger.log(TAG,
					"x::ContactPic::getContactPhotoFromNumber::NoSuchElementException::"
							+ e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.log(TAG,
					"x::ContactPic::getContactPhotoFromNumber::Image format not recognized::"
							+ e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(TAG,
					"x::ContactPic::getContactPhotoFromNumber::IOException::"
							+ e.getMessage());
			e.printStackTrace();
		}

		return photoObject;
	}

	/**
	 * This method converts the input stream into a gzip compressed byte array
	 * 
	 * @param input
	 *            input stream
	 * @return gzip byte array
	 * @throws IOException
	 */
	/*
	 * public byte[] gzipData(InputStream input) throws IOException {
	 * 
	 * ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 * GZIPOutputStream gzipos = new GZIPOutputStream(baos); readGzipData(input,
	 * gzipos); return baos.toByteArray(); }
	 * 
	 * private static void readGzipData(InputStream input, OutputStream os)
	 * throws IOException { byte[] buf = new byte[1024]; int len = 0;
	 * InputStream is = null; try { is = input; while ((len = is.read(buf)) > 0)
	 * { os.write(buf, 0, len); } //TODO: Error on J2ME, but I don't think its
	 * needed anyway. Same for the 2 GZIP methods! if (os instanceof
	 * DeflaterOutputStream) { ((DeflaterOutputStream) os).finish(); } } finally
	 * { if (is != null) { is.close(); } } }
	 */

	/**
	 * This method converts the byte stream from an input stream into a byte
	 * array
	 * 
	 * @param inputStream
	 *            file input stream
	 * @return input stream passed in the form of a byte array
	 * @throws IOException
	 *             the thrown exceptions need to be caught when using this
	 *             method
	 */
	/*
	 * public byte[] readBytes(InputStream inputStream) throws IOException {
	 * 
	 * // this dynamically extends to take the bytes you read
	 * ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream(); if(null
	 * != inputStream) { // this is storage overwritten on each iteration with
	 * bytes int bufferSize = 1024; byte[] buffer = new byte[bufferSize];
	 * 
	 * // we need to know how may bytes were read to write them to the
	 * byteBuffer int len = 0; while ((len = inputStream.read(buffer)) != -1) {
	 * byteBuffer.write(buffer, 0, len); } } else {byteBuffer.write(0);}
	 * 
	 * // and then we can return your byte array. return
	 * byteBuffer.toByteArray(); }
	 */

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
		if (true == containsOnlyNumbers(inputArgs[2])) {
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
						logger.log(TAG, "Sending Contact Photo Command..."); // set
																				// CRC
						resultREST = myServer
								.contactServer(
										flyContactPicMessage.getREST(),
										String.valueOf(myServer
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

	/**
	 * This method ensures that a string only contains numbers
	 * 
	 * @param str
	 *            the string to be checked
	 * @return true if the string contains a non-number character
	 */
	public boolean containsOnlyNumbers(String str) {

		// It can't contain only numbers if it's null or empty...
		if (str == null || str.length() == 0)
			return false;

		for (int i = 0; i < str.length(); i++) {

			// If we find a non-digit character we return false.
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}

		return true;
	}

	public void initialiseContactPic(LocalDataWriter inputLocalData) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method has been overridden from the Controllable interface. By
	 * implementing this interface this class can specify the type of commands
	 * it can process.
	 * 
	 * @param target
	 *            passed to be checked.
	 * @return true if this is the desired target.
	 */
	public boolean isTarget(COMMAND_TARGETS inputCOMMAND_TARGETS) {
		logger.log(TAG, "ContactPic::isTarget::COMMAND_TARGETS");
		if (inputCOMMAND_TARGETS == COMMAND_TARGETS.CONTACTS) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isTarget(String targets) {
		logger.log(TAG, "ContactPic::isTarget::String");
		if (targets == COMMAND_TARGETS.CONTACTS.toString()) {
			return true;
		} else {
			return false;
		}
	}
}