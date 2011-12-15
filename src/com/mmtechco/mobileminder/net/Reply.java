package com.mmtechco.mobileminder.net;

import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.MMLinkedList;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Transforms the data into easier accessible form.
 */
public class Reply {
	private static final String TAG = ToolsBB.getSimpleClassName(Reply.class);
	
	private static final MMTools tools = ToolsBB.getInstance();
	
	private String regID;
	private String restString;
	private boolean error;
	private String callingCODE;
	private String info;
	private Logger logger = Logger.getInstance();

	// Command Reply Class Variables
	private int index;
	private String target;
	private String args;

	/**
	 * Constructor: transforms the data from the web server into easier-access
	 * form for the registration class to use.
	 * 
	 * @param restMessage
	 *            a String contains the data from the web server.
	 */
	public Reply(String restMessage) {
		if (null != restMessage) {
			restString = restMessage;
			String[] replyArray;
			replyArray = tools.split(restString, Tools.ServerQueryStringSeparator);
			logger.log(TAG, "rest message: " + restMessage);

			try {
				if (0 < replyArray[1].length() // check if string is blank
						&& Integer.parseInt(replyArray[1]) == 0) // command
				{ // id,type,index,target,args
					int commandID = Integer.parseInt(replyArray[2]);

					if (0 == commandID)// id,type,comID,tag,arg -> 12345,00,0,,
					{
						initializeComReg(replyArray[0], replyArray[1],
								commandID, "", "");
					} else {
						initializeComReg(replyArray[0], replyArray[1],
								commandID, replyArray[3], replyArray[4]);
					}
				} else { // all others
					// id,calling code,error,info
					if (replyArray.length == 3) {
						logger.log(TAG, "ReplyArray length=3");
						initialize(replyArray[0], replyArray[1],
								Integer.parseInt(replyArray[2]) != 0, "");
					} else {
						try {
							logger.log(TAG, "ReplyArray length=4");
							initialize(replyArray[0], replyArray[1],
									Integer.parseInt(replyArray[2]) != 0,
									replyArray[3]);
						} catch (NumberFormatException e) {
							logger.log(TAG, "Reply: NumberFormatException: " + e);
						}
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.log(TAG, "Reply: ArrayIndexOutOfBoundsException: " + e);
			}
		}
	}

	/**
	 * Sets the value for Reply.
	 * 
	 * @param inputRegID
	 *            RegID for the device.
	 * @param inputError
	 *            error status.
	 * @param inputCallingCode
	 *            the event type.
	 * @param inputInfo
	 *            the body of the message.
	 */
	private void initialize(String inputRegID, String inputCallingCode,
			boolean inputError, String inputInfo) {
		regID = inputRegID;
		error = inputError;
		callingCODE = inputCallingCode;
		logger.log(TAG, "ReplyInfo: " + inputInfo);
		info = inputInfo;
	}

	/**
	 * Initializes a command message from the server.
	 * 
	 * @param inputRegID
	 *            device ID
	 * @param inputCallingCode
	 *            Type of message
	 * @param inputIndex
	 *            Index for command message
	 * @param indexTarget
	 *            target for command execution
	 * @param inputArgs
	 *            command to be executed
	 */
	private void initializeComReg(String inputRegID, String inputCallingCode,
			int inputIndex, String indexTarget, String inputArgs) {
		regID = inputRegID;
		callingCODE = inputCallingCode;
		index = inputIndex;
		target = indexTarget;
		args = inputArgs;
	}

	/**
	 * This method retrieves the error status of a reply message
	 * 
	 * @return true if an error occurred
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * This method retrieves the type of event message that was sent to the
	 * server
	 * 
	 * @return a single integer value representing the type of event.
	 */
	public String getCallingCode() {
		return callingCODE;
	}

	/**
	 * This method retrieves the information in the body of the message
	 * 
	 * @return the message body
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Retrieves the reply message formatted in to a single string value.
	 * 
	 * @return a single string containing the entire reply message.
	 */
	public String getREST() {
		return restString;
	}

	/**
	 * Retrieves the regID from the reply message
	 * 
	 * @return the regID. Returns the device identification number
	 */
	public String getRegID() {
		return regID;
	}

	/**
	 * Retrieves the Index from the reply message
	 * 
	 * @return the regID. Returns the index for the command message
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Retrieves the Target from the reply message
	 * 
	 * @return the regID. Returns the target for the command message
	 */
	public COMMAND_TARGETS getTarget() {
		return COMMAND_TARGETS.from(target);
	}

	/**
	 * Retrieves the Arguments from the reply message
	 * 
	 * @return the regID. Returns the argument of the command message
	 */
	public String[] getArgs() {
		logger.log(TAG, "Processing the args :" + args);
		String[] processedArgs = tools.split(args, "|");
		return processedArgs;
	}

	/**
	 * Splits an input string by its separators and returns each section as part
	 * of an array
	 * 
	 * @param inputCSV
	 *            input string
	 * @return array of values between separators
	 */
	public static String[] stringToArray(String inputCSV) {
		MMLinkedList tempList = new MMLinkedList();

		char[] tempCharList = inputCSV.toCharArray();
		int start = 0, end = 0;

		for (int count = 0; count < tempCharList.length; count++) {
			if (tempCharList[count] == ',') {
				if (start == end) {
					tempList.add(new String());
				} else {
					tempList.add(new String(inputCSV.substring(start, end)));
				}
				end++;
				start = end;
			} else {
				end++;
			}
		}
		// This accounts for the lasts value in the string, that will
		// not be detected within the loop that searches for commas
		if (start == end) {
			tempList.add(new String());
		} else {
			tempList.add(new String(inputCSV.substring(start, end)));
		}

		String[] returnArray = new String[tempList.size()];// =
															// tempList.toArray();
		tempList.toArray(returnArray);
		return returnArray;
	}
}
