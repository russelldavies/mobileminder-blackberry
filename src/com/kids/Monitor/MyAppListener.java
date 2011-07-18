package com.kids.Monitor;

import java.util.Date;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

import com.kids.Logger;
import com.kids.Registration;
import com.kids.Data.Tools;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

/**
 * 
 * MyAppListener monitors and registers application based events.
 *
 */

public class MyAppListener extends Thread
{
	private static final Debug				logger		= Logger.getInstance();;
	private 			 LocalDataReader	actLog;
	private 			 int				AppTimer;
	
/**
 * The AppListener constructor initialise the action store location and the interval value.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputAppTimer interval value
 */
	public MyAppListener(LocalDataReader inputAccess, int inputAppTimer)
	{
		actLog 		= inputAccess;
		AppTimer 	= inputAppTimer;
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
	{
		logger.log("MyAppListener begin...");
		try
	    {
			int lastProcessId = 0;
			ApplicationManager manager = ApplicationManager.getApplicationManager();
			//Date StartTimer  = new Date();
			String lastAppName = "BootUp Device";
			AppMessage appMessage=new AppMessage();
			ApplicationDescriptor runningApps[];
			
			while(true)
			{
				sleep(AppTimer);
				//Store a list of running apps
				runningApps = manager.getVisibleApplications();
				
				//If a new app is detected (by comparing current processID with processID from last loop)
				if(manager.getForegroundProcessId() != lastProcessId)
				{
					// Make this processID the current working ID
					lastProcessId = manager.getForegroundProcessId();
					// Search through the list of running apps for the app that matches this process ID
					for(int count = 0; runningApps.length > count; count++)
					{
						// When we find a match....
						if(manager.getProcessId(runningApps[count]) == lastProcessId)
						{
							lastAppName = runningApps[count].getName();
							logger.log("Current running app name is: "+lastAppName);
							logger.log("App found. Adding to log...");
							// ...add details of this app to the AppMessage object...
							appMessage.clearData();
							appMessage.setMessage(lastAppName,runningApps[count].getModuleName());	
							// ...and then to the database
							actLog.addMessage(appMessage);
							break;
						}  // End if()
					} // End for()
				}  // End if()
			}  // End while()
	    } // try
		catch (InterruptedException e)
		{
			logger.log("x::AppListener::run::InterruptedException::"+e.getMessage());
		}
       /* catch (Exception e)
		{
        	logger.log("x::ApplListener::run::Exception::"+e.getMessage());
        }*/
	}
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
			upTime			= 0;
			appName			= "";
			fullPackageName = "";
			launchTime 		= "";
			startTime 		= null;
			stringREST 		= null;
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
			stringREST.append('0');stringREST.append(getType());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(appName);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(getTime());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(upTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(fullPackageName);				
		}
		return 	stringREST.toString();
	}
}