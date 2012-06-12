package com.mmtechco.mobileminder.net;

import com.mmtechco.mobileminder.prototypes.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Transforms the data into easier accessible form.
 */
public class Reply {
	private static final String TAG = ToolsBB.getSimpleClassName(Reply.class);
	
	private static final MMTools tools = ToolsBB.getInstance();
	
	private String replyStr;
	
	private String id;
	private boolean error;
	private String type;
	private String info;

	// Command Reply Class Variables
	private int index;
	private String target;
	private String args;

	public Reply(String replyStr) throws Exception {
		if (null == replyStr) {
			return;
		}
		this.replyStr = replyStr;
		String[] replyArray;
		replyArray = tools.split(replyStr, Server.separator);

		// not blank and command
		if (0 < replyArray[1].length() && Integer.parseInt(replyArray[1]) == 0) {
			// id,type,index,target,args
			int commandID = Integer.parseInt(replyArray[2]);

			if (0 == commandID) {
				// id,type,comID,tag,arg -> 12345,00,0,,
				initializeComReg(replyArray[0], replyArray[1], commandID, "", "");
			} else {
				initializeComReg(replyArray[0], replyArray[1], commandID, replyArray[3], replyArray[4]);
			}
		} else {
			// all others
			// id,type,error,info
			if (replyArray.length == 3) {
				initialize(replyArray[0], replyArray[1], Integer.parseInt(replyArray[2]) != 0, "");
			} else {
				try {
					initialize(replyArray[0], replyArray[1], Integer.parseInt(replyArray[2]) != 0, replyArray[3]);
				} catch (NumberFormatException e) {
					Logger.log(TAG, "Reply: NumberFormatException: " + e);
				}
			}
		}
	}

	/**
	 * Sets the value for Reply.
	 * 
	 * @param regid
	 *            RegID for the device.
	 * @param error
	 *            error status.
	 * @param type
	 *            the event type.
	 * @param info
	 *            the body of the message.
	 */
	private void initialize(String id, String type, boolean error, String info) {
		this.id = id;
		this.type = type;
		this.error = error;
		this.info = info;
	}

	/**
	 * Initializes a command message from the server.
	 * 
	 * @param id
	 *            device ID
	 * @param type
	 *            Type of message
	 * @param index
	 *            Index for command message
	 * @param target
	 *            target for command execution
	 * @param args
	 *            command to be executed
	 */
	private void initializeComReg(String id, String type, int index, String target, String args) {
		this.id = id;
		this.type = type;
		this.index = index;
		this.target = target;
		this.args = args;
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
		return type;
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
		return replyStr;
	}

	/**
	 * Retrieves the regID from the reply message
	 * 
	 * @return the regID. Returns the device identification number
	 */
	public String getRegID() {
		return id;
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
		//logger.log(TAG, "Processing the args :" + args);
		String[] processedArgs = tools.split(args, "|");
		return processedArgs;
	}
}
