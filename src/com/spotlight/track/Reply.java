package com.spotlight.track;

import java.util.Vector;

import com.kids.prototypes.Debug;

/**
 * This class transforms the data into easier accessible form.
 */
public class Reply
{
	private String 	regID;
	private String  restString;
	private boolean error;
	private String  callingCODE;
	private String  info;
	private Debug logger = Logger.getInstance();
	
	//Command Reply Class Variables
	private int index;
	private String target;
	private String args;
	
/**
 * This is the constructor of Reply class. It transforms the data from the web server into easier-access form for the registration class to use.
 * @param restMessage a String contains the data from the web server.
 */
	public Reply(String restMessage)
	{
		
		restString = restMessage;
		String[]replyArray = Tools.split(restString, Server.RestElementSeparator);
		logger.log("rest messsage: "+restMessage);
		if(null != restMessage)
		{
			
		//	if(replyArray[3].equals("00"))

			//logger.log(replyArray[1]);
			
			try
			{
				if(replyArray[1].length() > 0 		//check if string is blank
							&& Integer.parseInt(replyArray[1])==0)	 //command
				{	//id,type,index,target,args
	
					int commandID = Integer.parseInt(replyArray[2]);
					if(0 == commandID)//id,type,comID,tag,arg -> 12345,00,0,,
						{inishliseCom(replyArray[0], replyArray[1],commandID , "","");}
					else
						{inishliseCom(replyArray[0], replyArray[1],commandID , replyArray[3],replyArray[4]);}
				}
				else//all others
				{	//id,calling code,error,info
					try
					{
					inishlise(replyArray[0],Integer.parseInt(replyArray[2])!=0,replyArray[1],replyArray[3]);
					}
					catch(NumberFormatException e)
					{logger.log("Reply: NumberFormatException: "+e);}
				}
			}
			catch(ArrayIndexOutOfBoundsException e)
			{	
				logger.log("Reply: ArrayIndexOutOfBoundsException: "+e);
				logger.log("StackTrace: "+e.getMessage());
				//actlog.addMessage(new ErrorMessage(e));
			}
		}

		
		//else
		//{	error = true;	}

	}
	

/**
 * This method sets the value for Reply.
 * @param inputRegID RegID for the device.
 * @param inputError error status.
 * @param inputCallingCode the event type.
 * @param inputInfo the body of the message.
 */
	private void inishlise(String inputRegID, boolean inputError, String inputCallingCode, String inputInfo)
	{
		regID		= inputRegID;
		error 		= inputError;
		callingCODE = inputCallingCode;
		info 		= inputInfo;
	}
	
/**
 * This method is used to initialise a command message from the server. 
 * 
 * @param inputRegID device ID
 * @param inputCallingCode Type of message
 * @param inputIndex Index for command message
 * @param indexTarget target for command execution
 * @param inputArgs command to be executed
 */
	private void inishliseCom(String inputRegID, String inputCallingCode, int inputIndex, String indexTarget, String inputArgs)
	{
		regID		= inputRegID;
		callingCODE = inputCallingCode;
		index 		= inputIndex;
		target 		= indexTarget;
		args 		= inputArgs;		
	}
	
	
/**
 * This method retrieves the error status of a reply message
 * 
 * @return true if an error occurred
 */
	public boolean isError()
	{	return error;	}
	
/**
 * This method retrieves the type of event message that was sent to the server 
 * 
 * @return a single integer value representing the type of event.
 */
	public String getCallingCode()
	{	return callingCODE;	}
	
/**
 * This method retrieves the information in the body of the message
 * 
 * @return the message body
 */
	public String getInfo()
	{	return info;	}
	
/**
 * Retrieves the reply message formatted in to a single string value.
 * 
 * @return a single string containing the entire reply  message.
 */
	public String getREST()
	{	return restString;	}
	
/**
 * Retrieves the regID from the reply message
 * 
 * @return the regID. Returns the device identification number
 */
	public String getRegID()
	{	return regID;	}
	
/**
 * Retrieves the Index from the reply message
 * 
 * @return the regID. Returns the index for the command message
 */
	public int getIndex()
	{	return index;	}
	
/**
 * Retrieves the Target from the reply message
 * 
 * @return the regID. Returns the target for the command message
 */
	public String getTarget()
	{
		return COMMAND_TARGETS.from(target);	
	}
	
/**
 * Retrieves the Arguments from the reply message
 * 
 * @return the regID. Returns the argument of the command message
 */
	public String[] getArgs()
	{	
		logger.log("Processing the args :"+args);
		//59_del/sdcard/download/police_mercedes_cls_brabus_rocket_img2.jpg
		int temp = args.indexOf('_');
		//temp = 2
		int offSet = Integer.parseInt(args.substring(0, temp));
		//59
		String command = args.substring(temp+1, args.length() - offSet);//2,65-59-1=5
		//_del
		String [] commandArray = Tools.split(command, "_");
		//[0][1]del
		String data = args.substring(args.length() - offSet ,args.length());
		//59-65 <-error
		String [] processedArgs = new String [(commandArray.length+1)];
		//
		for(int count=0; count < commandArray.length; count++)
		{
			processedArgs[count] = commandArray[count];
		}
		processedArgs[processedArgs.length-1] = data;
		
		logger.log("Processed args :"+processedArgs[processedArgs.length-1]);

		return processedArgs;
		
	}
	
/**
 * Splits an input string by its separators and returns each section as part of an array
 * @param inputCSV input string
 * @return array of values between separators
 */

	public static String[] stringToArray(String inputCSV)
	{
		//ArrayList<String> tempList = new ArrayList<String>();	
		Vector tempList = new Vector();
		//logger.log("stringToArray Arraylist inputCSV = "+inputCSV);

		char[] tempCharList = inputCSV.toCharArray();
		int start = 0, end = 0;

		for(int count = 0; count < tempCharList.length; count++)
		{			
			if (tempCharList[count] == ',')
			{
				if(start == end)
				{tempList.addElement(new String());}//.add(new String());}
				else
				{	tempList.addElement(inputCSV.substring(start, end));} 
				end++;
				start = end;
			}			
			else
			{	end++;	}
		
		}	
		//This accounts for the lasts value in the string, that will not be detected within the loop that searches for commas

		if(start == end)
		{tempList.addElement(new String());}
		
		else
		{	tempList.addElement(inputCSV.substring(start, end));}

			
		String[] returnArray = new String[tempList.size()];
		tempList.copyInto(returnArray);//.toArray(returnArray);

		return returnArray;
	}
}
