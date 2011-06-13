package com.spotlight.track;

import java.util.Date;

import com.kids.prototypes.Message;

//import com.kids.Controller;
//import com.kids.Registration;
//import com.kids.prototypes.Message;

/**
 * 
 * This class implements the message interface to hold call event messages.
 */
public class CallMessage implements Message
{
	private final String  type;	
	private String 		  deviceTime;	
	private Date		  startTime;
	private String 		  number;
	private int 		  duration;// in Sec
	private boolean 	  startStatus;//0:Incoming/1:Outgoing
	private byte 		  endStatus;
	private StringBuffer  stringREST;
	private String		  contactName;

/**
 * The constructor initialises a normal call message
 */
	public CallMessage()
	{	this(false);	}
	
/**
 * The constructor initialises a normal call message or a sync call message based on the boolean value passed to it.
 * 
 * @param sync true if sync message
 */
	public CallMessage(boolean sync)
	{	
		if(sync)
		{	type = "11";	}
		else
		{	type = "01";	}
		clearData();	
	}

/**
 * This method adds the call event information to the call message object.
 * 
 * @param _number the phone number received or dialled
 * @param _outgoing states whether the call was outgoing or incoming
 */
	public void setMessage(String _number, boolean _outgoing)
	{		
		number 		= _number;
		startStatus = _outgoing;
		//startTime = new Date();
		deviceTime 	= Tools.getDate();
	//	Controller.log("xxxxxx number:"+number);
	}

/**
 * This method adds the call event information to the call message object.
 * @param _number the phone number received or dialled.
 * @param _outgoing states whether the call was outgoing or incoming.
 * @param _deviceTime Time when the call begins.
 * @param _duration the length of the call.
 */
	public void setMessage(String _number, boolean _outgoing, String _deviceTime, int _duration)
	{		
		number 		= _number;
		startStatus = _outgoing;
		deviceTime 	= _deviceTime;
		duration 	= _duration;
		endStatus	= (byte)CallEndStatus.OTHER;//.ordinal();
		setEndStatus();
	}
	
/**
 * This method removes the current data in the message and initialises the parameters.
 * 
 */
	public void clearData()//This is used to ensure good practices and save resources on the device.
	{
		deviceTime 	= "";	
		number 		= "";
		duration 	= 0;
		startStatus = false;//Incoming
		endStatus	= (byte)CallEndStatus.OTHER;//.ordinal();
		startTime	= null;
		stringREST  = null;
		contactName = "";
	}

/**
 * This method retrieves the message formatted in to a single string value.
 * <p>
 * Call message consists of:
 * <ul>
 * <li> Registration Serial number.
 * <li> Call message type (two digits number).<br> '11' is used If call message is synchronised and '01' is used if message is not synchronised. 
 * <li> Time when the call begins.
 * <li> Phone Number ( received or dialled ).
 * <li> Contact Name.
 * <li> Call duration
 * <li> Call start status ( Incoming or outgoing )
 * <li> Call end status ( no-answered, finished etc )
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
			stringREST.append(number);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(contactName);
			//stringREST.append(Controller.getContactNameFromNumber(number));
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(duration);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(startStatus);
			stringREST.append(Server.RestElementSeparator);
			stringREST.append(endStatus);
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
 * This method records the start time of the call.
 */
	public void setStartTime()
	{startTime = new Date();}

	
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
 * This method calculates and records the duration of the call
 */
	public void setEndDuration()
	{duration = (int)(new Date().getTime()-startTime.getTime())/1000;}
	
	//TODO: Change spelling on all "sataus" to status below.
	
/**
 * This method is used to record "finished" as the way in which the call was ended. 
 */
	public void setEndStatus()
	{setEndStatus(CallEndStatus.FINISHED);}
	
/**
 * This method records the status in which the call was ended.
 * 
 * @param _status the status in which the call was ended
 */
	public void setEndStatus(byte _status/*CallEndStatus _status*/)
	{endStatus = _status;/*(byte)_status.ordinal();*/}
}
