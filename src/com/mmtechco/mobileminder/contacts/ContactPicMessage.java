package com.mmtechco.mobileminder.contacts;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

/**
 * 
 * This class implements the message interface to hold ContactPic messages.
 */
class ContactPicMessage implements Message {

	private final int type = 28;
	private String fileType;
	private String contactNumber;
	private String contactEmail;
	private MMTools tools = ToolsBB.getInstance();

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
		return 
				Registration.getRegID() +
				Tools.ServerQueryStringSeparator +
				type +
				Tools.ServerQueryStringSeparator +
				tools.getDate() +
				Tools.ServerQueryStringSeparator +
				fileType +
				Tools.ServerQueryStringSeparator +
				getContactNameFromNumber(contactNumber) +
				Tools.ServerQueryStringSeparator +
				contactNumber +
				Tools.ServerQueryStringSeparator +
				contactEmail;
	}

	private String getContactNameFromNumber(String _contactNum) {
		String thePhoneNumber = null;
		PhoneCallLogID callLog = new PhoneCallLogID(_contactNum);
		if (callLog.getName() != null) {
			thePhoneNumber = callLog.getName();
		}
		return thePhoneNumber;
	}

	public String getTime() {
		return "?";
	}
}