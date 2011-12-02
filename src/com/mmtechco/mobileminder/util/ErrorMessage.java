package com.mmtechco.mobileminder.util;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;

public class ErrorMessage implements Message {
	private static final int type = 7;
	
	private MMTools tools = ToolsBB.getInstance();

	private String errorType = "";
	private String errorClass = "";
	private String errorLineNumber = "";
	private String errorPackage = "";
	private long deviceUpTime = 0;
	private String deviceTime;

	public ErrorMessage(Exception inputE) {
		clearData();
		setMessage(inputE);
	}

	public ErrorMessage(Throwable throwableInputE) {
		clearData();
		setMessage(throwableInputE);
	}

	/**
	 * This method adds the call event information to the call message object
	 * 
	 * @param inputError
	 *            specifies if an error has occurred
	 * @param inputNumber
	 *            the phone number received or dialled
	 * @param outgoing
	 *            states whether the call was outgoing or incoming
	 */
	public void setMessage(Exception inputE) {
		// Gets the stack trace
		errorType = inputE.getMessage();
		deviceUpTime = tools.getUptimeInSec();
		deviceTime = tools.getDate();
		
		inputE.printStackTrace();
	}

	public void setMessage(Throwable inputE) {
		// gets the stack trace
		errorType = inputE.getMessage();

		deviceUpTime = tools.getUptimeInSec();
		deviceTime = tools.getDate();

		inputE.printStackTrace();
	}

	/**
	 * This method removes the current data in the message and initializes the
	 * parameters.
	 * 
	 */
	public void clearData()// This is used to ensure good practices and save
							// resources on the device.
	{
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
	 * <li>Registration Serial number.
	 * <li>Error message type which is '07' (two digits number).
	 * <li>Device Time when the error is occurred.
	 * <li>Class name in which error is occurred.
	 * <li>Package name in which error is occurred.
	 * <li>Line number in class in which error is occurred.
	 * <li>Type of the error.
	 * <li>Time of device since it is turned on till error is occurred.
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return
			Registration.getRegID() +
			Tools.ServerQueryStringSeparator + 
			"0" +
			type + 
			Tools.ServerQueryStringSeparator + 
			tools.getDate()+
			Tools.ServerQueryStringSeparator + 
			Tools.ServerQueryStringSeparator + 
			errorClass +
			Tools.ServerQueryStringSeparator +
			errorPackage +
			Tools.ServerQueryStringSeparator +
			errorLineNumber +
			Tools.ServerQueryStringSeparator +
			errorType +
			Tools.ServerQueryStringSeparator +
			deviceUpTime;
	}

	/**
	 * This method retrieves the time that is set on the device.
	 * 
	 * @return the device time
	 */
	public String getTime() {
		return deviceTime;
	}

	/**
	 * This method retrieves the type number for the call message
	 * 
	 * @return the type number corresponding to a call message
	 */
	public int getType() {
		return type;
	}

}