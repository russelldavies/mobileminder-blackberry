package com.spotlight.track;

import java.util.Date;

import com.kids.Data.Tools;
import com.kids.net.Server;
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
	private String  	  deviceTime;
	private StringBuffer  stringREST;
	private byte    	  sentStatus; //0:Incoming/1:Outgoing
	private boolean		  hasAttachment;
	private Date		  sentTime;

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
		contactEmail 	= _email;
		sentStatus 		= _outgoing;
		deviceTime 		= Tools.getDate();
	//	Controller.log("xxxxxx number:"+number);
	}

	/**
	 * This method adds the mail event information to the mail message object.
	 * @param _email the contacts email address
	 * @param _outgoing states whether the mail was outgoing or incoming.
	 * @param _deviceTime Time when the mail was sent.
	 * @param _hasAttachment boolean indicating if the email had an attachment
	 */
	public void setMessage(String _email, byte _outgoing, String _deviceTime, boolean attachment)
	{		
		contactEmail 	= _email;
		sentStatus 		= _outgoing;
		deviceTime 		= _deviceTime;
		hasAttachment	= attachment;
		//setDirectionStatus();
	}
	
	/**
	 * This method removes the current data in the message and initialises the parameters.
	 * 
	 */
	public void clearData()//This is used to ensure good practices and save resources on the device.
	{
		deviceTime 		= "";	
		contactEmail 	= "";
		stringREST  	= null;
		contactName 	= "";
		hasAttachment	= false;
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
	//@Override
	public String getREST() 
	{
		
	//	Controller.log("xxxxxx number:"+number);
		if(null == stringREST)
		{	
			stringREST = new StringBuffer();
			stringREST.append(Registration.getRegID());
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(type);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(deviceTime);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(contactEmail);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(contactName);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(sentTime);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(sentStatus);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(hasAttachment);
			stringREST.append(Server.RestElementSeparator);
			/*								
		   stringREST = Registration.getRegID() + Server.RestElementSeparator +
						type 					+ Server.RestElementSeparator +
						deviceTime				+ Server.RestElementSeparator +
						number					+ Server.RestElementSeparator +
						duration				+ Server.RestElementSeparator +
						startStatus				+ Server.RestElementSeparator +
						endSataus				+ Server.RestElementSeparator +
						info;*/
		}		
		// RegSN,	call, deviceTime, number,	duration, startStatus,	endSataus, info
		return 	stringREST.toString();			
		//return null;
	}

	/**
	 * This method records the sent time of the email.
	 */
	public void setSentTime()
	{sentTime = new Date();}

	
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
	//@Override
	public String getTime() 
	{	return deviceTime;	}
	
	/**
	 * This method retrieves the type number for the call message
	 * 
	 * @return the type number corresponding to a call message
	 */
	//@Override 
	public int getType() 
	{	return Integer.parseInt(type);	}
	
	
	/**
	 * This method sets the email as inbound
	 * 
	 */
	public void setInboundStatus()
	{sentStatus = MailDirectionStatus.INBOUND;}
	
	/**
	 * This method sets te email as outbound
	 */
	public void setOutboundStatus()
	{sentStatus = MailDirectionStatus.OUTBOUND;}
}
