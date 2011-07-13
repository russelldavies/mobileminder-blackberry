package com.kids.Monitor;

import java.util.Date;

import com.kids.Logger;
import com.kids.Registration;
import com.kids.Data.Tools;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

/**
 * 
 * MyAppListener monitors and registers application based events.
 *
 */

public class MyAppListener extends Thread
{
	private LocalDataWriter actLog;
	private int AppTimer;
	Debug log = Logger.getInstance();
	//AppMessage appMessage;
	
/**
 * The AppListener constructor initialise the action store location and the interval value.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputAppTimer interval value
 */
	public MyAppListener(LocalDataWriter inputAccess, int inputAppTimer)
	{
		actLog = inputAccess;
		AppTimer = inputAppTimer;
		this.start();
	}

/**
* 
* 		The Application Listener class stores the application currently running on the device at specific intervals in the action log in the form of an action of type app.
* <p>
* 		The inherited run method is called when the constructor starts, the function obtains the id of the application running in the foreground on the device. 
* 		It compares the id to the application heap. 
* 		The application heap holds the application that have been running on the device stored on the devices memory
* 		This consists of the application id's with the corresponding name of the application.
* 		The application heap is searched using the foreground application id to find the name of the application.
* 		This is then stored to the action log as an action of type App.
*
*/
	
	public void run()
	{/*
		log.log("MyAppListener begin...");
		try
	    {
			int lastProcessId = 0;
			ApplicationManager manager = ApplicationManager.getApplicationManager();
			Date StartTimer  = new Date();
			String lastAppName = "BootUp Device";
			AppMessage appMessage=new AppMessage();
			
			while(true)
			{
				this.sleep(AppTimer);
				ApplicationDescriptor visibleApplications[] = manager.getVisibleApplications();
				
				if(manager.getForegroundProcessId() != lastProcessId)
				{
					lastProcessId = manager.getForegroundProcessId();					
					for(int count = 0; visibleApplications.length > count; count++)
					{ 
						if(manager.getProcessId(visibleApplications[count]) == lastProcessId)
						{ 
							//actLog.addMessage(action.TYPE_APP,lastAppName+
							//		":"+(int)(new Date().getTime()-StartTimer.getTime())/1000);
							appMessage.setMessage(lastAppName,visibleApplications[count].getModuleName());							
							actLog.addMessage(appMessage);
							StartTimer = new Date();
							lastAppName = visibleApplications[count].getName();
							break;
						}  // End if()
					} // End for()
				}  // End if()
			}  // End while()
	    } // try
		catch (InterruptedException e)
        // In Legacy, its (overloaded )addAction(bool,int,String,String)
        // In Local its addMessage(Message)
		{
        	actLog.addMessage(myMessage);//(true,action.TYPE_APP,e.toString());}
		}
        catch (Exception e)
		{
        	actLog.addMessage(true,action.TYPE_APP,e.toString());
        }
	*/}
}


/**
 * This class implements the message interface to hold application information.
 * 
 **/
class AppMessage implements Message
{
	private final int 	 type = 5;
	private int 		 upTime;
	private String 		 launchTime;
	private String 		 appName;
	private String 		 fullPackageName;
	private StringBuffer stringREST;
	private Date		 startTime;
	private MMTools 	 tools = Tools.getInstance();

			
/**
 * The constructor initialises all the message parameters
 */
		public AppMessage()
		{clearData();}
		
/**
 * This method adds the application information to the application message object
 * @param _appName application name
 * @param _packageName application package name
 */	
		public void setMessage(String _appName, String _packageName)
		{
			appName = _appName;
			launchTime = tools.getDate();
			startTime = new Date();//get the time now
			fullPackageName = _packageName;
		}
		
/**
 * This method calculates and records the duration of the application
 */
		public void setEndDuration()
		{upTime = (int)(new Date().getTime()-startTime.getTime())/1000;}
		
/**
 * This method removes the current data in the message and initialises the parameters.
 * 
 */
		public void clearData()//This is used to ensure good practices and save resources on the device.
		{
			upTime		= 0;
			appName		= "";
			fullPackageName = "";
			launchTime = "";
			startTime = null;
			stringREST = null;
		}
			
/**
 * This method retrieves the type number for the application message
 * 
 * @return the type number corresponding to an application message
 */
		//@Override 
		public int getType() 
		{return type;}
			
/**
 * This method retrieves the launch time of the application
 * 
 * @return the application launch time
 */
		//@Override 
		public String getTime() 
		{return launchTime;}
			
/**
 * This method retrieves the message formatted in to a single string value.
 * <p>
 * App message consists of:
 * <ul>
 * <li> Registration Serial number.
 * <li> Type of App message which is '05' (two digit number).
 * <li> Application Name.
 * <li> Launching time of application.
 * <li> Application Up-Time.
 * <li> Application Full Peckage Name.
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
			stringREST.append('0');stringREST.append(type);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(appName);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(launchTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(upTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(fullPackageName);				
		}
		return 	stringREST.toString();
	}
}
