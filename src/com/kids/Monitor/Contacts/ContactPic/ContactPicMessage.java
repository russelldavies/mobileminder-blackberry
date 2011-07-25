package com.kids.Monitor.Contacts.ContactPic;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

import com.kids.Registration;
import com.kids.Data.Tools;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;


/**
 * 
 * This class implements the message interface to hold ContactPic messages.
 */
class ContactPicMessage implements Message
{
	
	private final int type = 28;
	private String	fileType;
	private String	contactNumber;
	private String	contactEmail;
	private StringBuffer stringREST;
	private MMTools tools = Tools.getInstance();

	
	public ContactPicMessage()
	{
		clearData();
	}
	
	/**
	 * This method sets the parameters for the ContactPic message to be sent
	 * 
	 * @param inputFileType file type of the picture
	 * @param inputContactNumber number of the contact
	 * @param inputContactEmail email address stored in the contact information
	 */
	public void setMessage(String inputFileType, String inputContactNumber, String inputContactEmail)
	{
		fileType=inputFileType;
		contactNumber=inputContactNumber;
		contactEmail=inputContactEmail;
	}
	
	public void clearData()
	{
		fileType = null;
		contactNumber= null;
		contactEmail= null;
		stringREST  = null;
	}
	
	//@Override
	public int getType() {
		return type;
	}

	//@Override
	public String getTime() {
		// TODO Auto-generated method stub
		return "?";//null;
	}

	/**
	 * This method retrieves the ContactPic message formatted into a single string value.
	 * Web message consists of:
	 * <ul>
	 * <li> Registration Serial number.
	 * <li> ContactPic message type which is '28' (two digits number).
	 * <li> Contact picture file type. (e.g; JPEG, PNG, BMP etc )
	 * <li> Contact Name.
	 * <li> Contact Number.
	 * <li> Contact Email.
	 * </ul>
	 * @return a single string containing the ContactPic entire message.
	 */
	//@Override
	public String getREST() 
	{		
		if(null == stringREST)
		{	
			stringREST = new StringBuffer();
			stringREST.append(Registration.getRegID());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(type);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(tools.getDate());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(fileType);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(getContactNameFromNumber(contactNumber));
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(contactNumber);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(contactEmail);
		}		
		return 	stringREST.toString();		
	}

	private String getContactNameFromNumber(String _contactNum)
	{
		String thePhoneNumber = null;
		PhoneCallLogID callLog = new PhoneCallLogID(_contactNum);
        if (callLog.getName() != null)
        {
        	thePhoneNumber = callLog.getName();
        }
		return thePhoneNumber;
	}
}