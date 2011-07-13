package com.kids;

import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;
/**
 * 
 * This class implements the message interface to hold mail event messages.
 */
public class MailMessage implements Message
{
	private final String  type="3";		// 3 = Email message
	private String  	  contactEmail;
	private String  	  contactName;
	private String  	  emailSubject;
	private String		  emailBody;
	private String  	  deviceTime;
	private StringBuffer  stringREST;
	private byte    	  sentDirection; //0:Incoming/1:Outgoing
	private boolean		  hasAttachment;
	private String		  sentTime;
	private MMTools 	  tools = Tools.getInstance();

	/**
	 * The constructor initialises a normal mail message -> "01"
	 */
	public MailMessage()
	{	clearData();	}
	

	/**
	 * This method adds the email event information to the mail message object.
	 * 
	 * @param _address the email was sent to or received from
	 * @param _outgoing states whether the mail was outgoing or incoming
	 */
	public void setMessage(String _email, byte _outgoing)
	{		
		contactEmail 		= _email;
		sentDirection 		= _outgoing;
		deviceTime 			= tools.getDate();
	}

	/**
	 * This method adds the mail event information to the mail message object.
	 * @param _email the contacts email address
	 * @param _outgoing states whether the mail was outgoing or incoming.
	 * @param _deviceTime Time when the mail was sent.
	 * @param _hasAttachment boolean indicating if the email had an attachment or not
	 */
	public void setMessage(String _email, String _contact, String _emailSubject, String _mailBody, byte _outgoing, String _deviceTime, boolean _hasAttachment)
	{		
		contactEmail	= _email;
		contactName		= _contact;
		emailSubject	= _emailSubject;
		emailBody		= _mailBody;
		sentDirection	= _outgoing;
		sentTime		= _deviceTime;
		hasAttachment	= _hasAttachment;
	}
	
	
	/**
	 * This method removes the current data in the message and initialises the parameters.
	 * 
	 */
	public void clearData()//This is used to ensure good practices and save resources on the device.
	{
		deviceTime 		= null;	
		contactEmail 	= null;
		stringREST  	= null;
		contactName 	= null;
		hasAttachment	= false;
		sentTime		= null;
	}

	/**
	 * This method retrieves the message formatted in to a single string value.
	 * <p>
	 * Mail message consists of:
	 * <ul>
	 * <li> Registration Serial number.
	 * <li> Mail message type (two digits number).<br> '11' is used If call message is synchronised and '01' is used if message is not synchronised. 
	 * <li> Time on the device when the mail was sent.
	 * <li> Contact Email address.
	 * <li> Contact Name.
	 * <li> Sent Time of the mail itself
	 * <li> Mail sent status ( Incoming or outgoing )
	 * <li> Attachment present (true,false)
	 * </ul>
	 * @return a single string containing the entire message.
	 */
	public String getREST() 
	{
		if(null == stringREST)
		{	
			stringREST = new StringBuffer();
			stringREST.append(Registration.getRegID());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(type);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(deviceTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(contactEmail);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(contactName);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(emailSubject);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(emailBody);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(sentTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(sentDirection);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append( (hasAttachment)?(byte)1:(byte)0);
			stringREST.append(Tools.RestElementSeparator);
		}
		return 	stringREST.toString();			
	}

	/**
	 * This method records the sent time of the email.
	 */
	public void setSentTime(String _sentTime)
	{sentTime = _sentTime;}
	
	/**
	 * This method records the contacts name from the phone call.
	 */
	public void setContactName(String name)
	{contactName=name;}	
	
	/**
	 * This method retrieves the time that is set on the device.
	 * 
	 * @return the device time
	 */
	public String getTime() 
	{	return deviceTime;	}
	
	/**
	 * Returns the time at which the message was sent
	 * @return The time the email was sent
	 */
	public String getSentTime()
	{	return sentTime;	}
	
	/**
	 * This method retrieves the type number for the call message
	 * 
	 * @return the type number corresponding to a call message
	 */
	public int getType() 
	{	return Integer.parseInt(type);	}
	

	/**
	 * Sets the direction of the email, ie inbound (received), or outbound (sent).
	 * @param isInbound true for inbound, false for outbound
	 */
	public void setMailDirection(boolean isInbound)
	{sentDirection = (isInbound ? MailDirectionStatus.INBOUND : MailDirectionStatus.OUTBOUND);}
}