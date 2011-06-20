package com.kids;

import com.kids.Registration;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

/**
 * This class implements the message interface to hold SMS event messages.
 */
public class SMSMessage implements Message
{
	private final 	String 	type;
	private 		StringBuffer stringREST;
	private 		boolean startStatus;//0:Incoming/1:Outgoing
	private 		boolean endSataus;	//0:bounced /1:delivered
	private 		String 	number;
	private			String	contactName;
	private 		String 	deviceTime;
	private 		String 	info;
	//private MMTools tools = Tools.getInstance();
	
/**
 * The constructor initialises all the message parameters
 */
	public SMSMessage()
	{	this(false);	}
	
/**
 * The constructor initialises all the message parameters
 */
	public SMSMessage(boolean sync)
	{	
		if(sync)
		{	type = "12";	}
		else
		{	type = "02";	}
		clearData();	
	}
	
/**
 * Adds the SMS event information to the SMS message object
 * 
 * @param _number the phone number of the incoming or outgoing message
 * @param _outgoing states whether the SMS was outgoing or incoming
 * @param _inputBody The body of the SMS message
 */
	public void setMessage(String _number, boolean _outgoing, String _inputBody)
	{setMessage(_number, _outgoing,Tools.getDate() ,_inputBody);}
	
/**
 * Adds the SMS event information to the SMS message object
 * @param _number the phone number of the incoming or outgoing message
 * @param _outgoing states whether the SMS was outgoing or incoming
 * @param _deviceTime Time when the SMS is being made.
 * @param inputBody The body of the SMS message
 */
	public void setMessage(String _number, boolean _outgoing, String _deviceTime, String inputBody)
	{
		clearData();
		startStatus = _outgoing;//0:Incoming/1:Outgoing
		number 		= _number;
		deviceTime	= _deviceTime;
		info 		= inputBody;
	}
	
/**
 * This method removes the current data in the message and initialises the parameters. 
 */
	public void clearData()
	{
		startStatus = false;//Incoming
		endSataus 	= true; //delivered
		stringREST 	= null;
		info 		= "";
	}

/**
 * This method retrieves the information in the body of the message
 * @return the message body
 */
	public String getInfo() 
	{	return info;	}
	
/**
 * This method retrieves the message formatted in to a single string value.
 * <p>
 * SMS message consists of:
 * <ul>
 * <li> Registration Serial number.
 * <li> Type of SMS message.
 * <li> Device time.
 * <li> Phone number
 * <li> Status ( Incoming or outgoing ).
 * <li> Status ( Delivered or Bounced )
 * <li> The body of the SMS message.
 * </ul>
 * @return a single string containing the entire message.
 */
	//@Override
	public String getREST() 
	{
		//RegSN,	text, 	error, deviceTime, number,	startStatus,	endSataus,	info
		if(null == stringREST)
		{
			stringREST = new StringBuffer();
			stringREST.append(Registration.getRegID());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(type);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(deviceTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(number);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(contactName);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(startStatus);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(endSataus);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(info);
			/*
			stringREST = Registration.getRegID() + Tools.RestElementSeparator +
					type 					 + Tools.RestElementSeparator +
					(error?1:0)				 + Tools.RestElementSeparator +
					deviceTime				 + Tools.RestElementSeparator +
					number					 + Tools.RestElementSeparator +
					startStatus				 + Tools.RestElementSeparator +
					endSataus				 + Tools.RestElementSeparator +
					info;*/
		}
		// TODO Auto-generated method stub
		return stringREST.toString();
	}
	
/**
 * This method retrieves the time that is set on the device.
 * 
 * @return the device time
 */
	//@Override
	public String getTime() 
	{	return deviceTime;	}
	
/**
 * This method retrieves the type number for the SMS message
 * 
 * @return the type number corresponding to a SMS message
 */
	//@Override
	public int getType() 
	{	return Integer.parseInt(type);	}
	
	public void setContactName(String theName)
	{	contactName=theName;	}
	
}