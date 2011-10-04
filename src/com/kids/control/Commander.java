package com.kids.control;

import java.util.Vector;

import com.kids.Controllable;
import com.kids.Logger;
import com.kids.Registration;
import com.kids.Data.ToolKit;
import com.kids.net.Reply;
import com.kids.net.Server;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

/**
 * Responsible for contacting server and getting response from server.
 * It is also responsible for processing response. 
 */
public class Commander extends Thread 
{
	private Server 					myServer;
	//private ArrayList<Controllable> componentList;
	private Vector					componentList;
	private final int 				time;
	private LocalDataReader 		actLog;
	private final int 				commandSignal = 0;
	private Debug 					logger = Logger.getInstance();
	private MMTools 				tools;
	//CommandMessage commandMessage;
	//AppControl appControl;

/**
 * Used by Controller to initialises the {@link LocalDataAccess} object and {@link Controllable} objects.
 */
	//public Commander(LocalDataReader _actlog, Controller _controller, ArrayList<Controllable> _components)
	public Commander(LocalDataReader _actlog, /*Controller _controller,*/ Vector _components)
	{
		myServer = new Server(_actlog);
		//myServer.start();
		//time = 1000 * 60 * 5; //5mins
		//time = 1000 * 60 * 1; //30secs
		time = 1000 * 20;
		componentList = _components;
		actLog = _actlog;
		//commandMessageFirst = new CommandMessage();
		//	appControl = new AppControl();	
	}
	
/**
 * Contacts to server and gets reply from server. If reply is process-able then process it.
 */
	public void run()
	{	
		while(true)
		{
				boolean commandQueued = true;
				while(commandQueued)
				{
				//e.g. #app - BLOCK_com.android.alarmclock_1430_1800_weekday_1
					
					//1 send message to server to check if any tasks
					
					CommandMessage commandMessageFirst = new CommandMessage();
					commandMessageFirst.setMessage(commandSignal); // Publishing provider browser: com.android.browser.BrowserProvider
					//Reply myReply = new Reply("12347,0,64,APP,BLOCK_com.android.alarmclock_1600_1900_weekdays_1_45.0_55.0_5000");
					//Reply myReply = new Reply("12349,0,32,FILES,FILE_DEL_/sdcard/download/images.jpeg");					
					Reply myReply = myServer.contactServer(commandMessageFirst);

					//commandMessage.clearData();

					logger.log("COM Index:"+myReply.getIndex());
					
					//0 if tasks
					if(commandSignal == myReply.getIndex() || myReply.isError())
					{
						commandQueued = false;
						logger.log("No Commands to Process");
						break;
					}
					else
					{
						for(int count = 0; count <componentList.size(); count++)
						{
							CommandMessage commandMessage = new CommandMessage();
							
							Controllable aTarget = (Controllable) componentList.elementAt(count);//.get(count);
							
							if(aTarget.isTarget(myReply.getTarget()))
							{
								commandMessage.setStartTime();
								if(aTarget.processCommand(myReply.getArgs()))
								{	//ran fine :)
									
									logger.log("Sending Command Reply=ran fine} index:"+myReply.getIndex());
									
									commandMessage.setEndTime();
									commandMessage.setMessage(myReply.getIndex(), true);
									
									logger.log("Sending Command Reply Message:index="+myReply.getIndex()+" REST:"+commandMessage.getREST());

									myServer.contactServer(commandMessage);
								}
								else
								{	//No joy :(
									
									logger.log("Sending Command Reply=no joy} index:"+myReply.getIndex());
									
									commandMessage.setEndTime();
									commandMessage.setMessage(myReply.getIndex(), false);
									myServer.contactServer(commandMessage);
								}
								
								logger.log("Out of command reply= clearing data} index:"+myReply.getIndex());
								//flyCommandMessage.clearData();
								break;
							}
						}
					}
					commandQueued = false;//Break of TESTING App Controller
				}	
			try 
			{	Thread.sleep(time);} 
			catch (InterruptedException e)
			{
				//TODO:actLog.addMessage(new ErrorMessage(e));
			}		
		}	
	}
	


/**
 * 
 * This class implements the message interface to hold command message.
 */
	class CommandMessage implements Message
	{	
		private final int type = 0;
		private int index;
		private boolean completed;
		private String	startTime;
		private String	endTime;
		private StringBuffer stringREST;

/**
 * Initialises all the command message parameters
 */
		public CommandMessage()
		{clearData();}
		
/**
 * Adds command message information to command message.
 * @param _index index of command message
 * @param _completed sets true if the command reply ran fine, false otherwise.
 */
		public void setMessage(int _index, boolean _completed)
		{
			index = _index;
			completed = _completed;
		}

/**
 * Adds command message information to command message.
 * @param _index index of command message
 */
		public void setMessage(int _index)
		{index = _index;}
		
/**
 * Initialises all the command message parameters.
 */
		public void clearData()
		{
			index = 0;
			completed = false;
			startTime = "0";
			endTime = "0";
		}
		
/**
 * Returns the type of the command message.
 * @return type of the command message
 */
		public int getType()
		{  return type;  }
		
/**
 * Retrieves the time when command processing starts.
 */
		public String getTime() 
		{	return startTime; }
			
/**
 * Retrieves the message formatted in to a single string value.
 * Command message consists of:
 * <ul>
 * <li> Registration Serial number.
 * <li> Command message type which is '0'.
 * <li> Index of command message.
 * <li> Boolean true if command message ran fine, false otherwise.
 * <li> Start time of command message.
 * <li> End time of command message.
 * </ul>
 * @return a single string containing the entire message.
 */
		public String getREST() 
		{		
			if(null == stringREST)
			{	
				stringREST = new StringBuffer();
				stringREST.append(Registration.getRegID());
				stringREST.append(ToolKit.RestElementSeparator);
				stringREST.append('0'); stringREST.append(type);
				stringREST.append(ToolKit.RestElementSeparator);
				stringREST.append(index);
				stringREST.append(ToolKit.RestElementSeparator);
				stringREST.append((completed)?1:0);
				stringREST.append(ToolKit.RestElementSeparator);
				stringREST.append(startTime);
				stringREST.append(ToolKit.RestElementSeparator);
				stringREST.append(endTime);
			}		
			
			return 	stringREST.toString();				
		}
		
/**
 * Sets the time when the command started to be processed.
 */
		public void setStartTime()
		{startTime = tools.getDate();}
		
/**
 * Retrieves the time when the command started to be processed.
 * @return Time when the command started to be processed.
 */
		public String getStartTime()
		{return startTime;}
		
/**
 * Sets the current time when the command finished processing.
 */
		public void setEndTime()
		{endTime = tools.getDate();}
/**
 * Gets the current time when the command finished processing.
 * @return Time when the command finished processing
 */
		public String getEndTime()
		{return endTime;}	  
	}	
}