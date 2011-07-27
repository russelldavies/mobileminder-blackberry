package com.kids.net;

import com.kids.Logger;
import com.kids.Registration;
import com.kids.Data.Tools;
import com.kids.prototypes.Debug;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

public class ErrorMessage implements Message
{	
	private final int type = 7;
	private StringBuffer stringREST;
	private Debug logger = Logger.getInstance();
	private MMTools tools = Tools.getInstance();

	private static String errorType = "";
	private static String errorClass = "";
	private static String errorLineNumber = "";
	private static String errorPackage = "";
	private static int deviceUpTime = 0;
	private static String deviceTime;

/**
 * The constructor initialises all the message parameters
 */
	//@SuppressWarnings("unused")
	private ErrorMessage()
	{	clearData();	}
	
/**
 * initialises all the message parameters
 * @param inputE An exception
 */
	public ErrorMessage(Exception inputE)
	{	
		clearData();	
		setMessage(inputE);
	}
	public ErrorMessage(Throwable throwableInputE)
	{	
		clearData();
		setMessage(throwableInputE);
	}
	
/**
 * This method adds the call event information to the call message object
 * 
 * @param inputError specifies if an error has occurred 
 * @param inputNumber the phone number received or dialled
 * @param outgoing states whether the call was outgoing or incoming
 */
	public void setMessage(Exception inputE)
	{
		//gets the stack trace
		errorType = inputE.getMessage();
		
		deviceUpTime = tools.getUptimeInSec();
		deviceTime 	 = tools.getDate();
	}
	
	
	public void setMessage(Throwable inputE)
	{
		//gets the stack trace
		errorType = inputE.getMessage();
		
		deviceUpTime = tools.getUptimeInSec();
		deviceTime 	= tools.getDate();
	}
	
	
/**
 * This method removes the current data in the message and initializes the parameters.
 * 
 */
	public void clearData()//This is used to ensure good practices and save resources on the device.
	{
		stringREST  = null;
		errorType = "";
		errorClass = "";
		errorLineNumber = "";
		errorPackage = "";
		deviceUpTime = 0;
	}

/**
 * This method retrieves the message formatted in to a single string value.
 * Error message consists of:
 * <ul>
 * <li> Registration Serial number.
 * <li> Error message type which is '07' (two digits number).
 * <li> Device Time when the error is occurred.
 * <li> Class name in which error is occurred.
 * <li> Package name in which error is occurred.
 * <li> Line number in class in which error is occurred.
 * <li> Type of the error.
 * <li> Time of device since it is turned on till error is occurred.
 * </ul>
 * @return a single string containing the entire message.
 */
	//@Override
	public String getREST() 
	{
		if(null == stringREST)
		{	
			stringREST = new StringBuffer();
			stringREST.append(Registration.getRegID());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append('0');	stringREST.append(type);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(deviceTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(errorClass);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(errorPackage);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(errorLineNumber);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(errorType);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(deviceUpTime);
		}
		return 	stringREST.toString();
	}

/**
 * This method retrieves the time that is set on the device.
 * 
 * @return the device time
 */
	//@Override
	public String getTime()	
	{	return  deviceTime;	}
	
/**
 * This method retrieves the type number for the call message
 * 
 * @return the type number corresponding to a call message
 */
	//@Override 
	public int getType() 
	{	return type;	}

}