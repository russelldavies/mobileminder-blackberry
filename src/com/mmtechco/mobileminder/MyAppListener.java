package com.mmtechco.mobileminder;

import java.util.Date;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;


/**
 * 
 * MyAppListener monitors and registers application based events.
 *
 */

public class MyAppListener extends Thread
{
	private LocalDataAccess actLog;
	private int AppTimer;
	
/**
 * The AppListener constructor initialise the action store location and the interval value.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputAppTimer interval value
 */
	public MyAppListener(LocalDataAccess inputAccess, int inputAppTimer)
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
	{
		try
	    {
			int lastProcessId = 0;
			ApplicationManager manager = ApplicationManager.getApplicationManager();
			Date StartTimer  = new Date();
			String lastAppName = "BootUp Device";
			
			while(true)
			{
				Thread.sleep(AppTimer);
				ApplicationDescriptor visibleApplications[] = manager.getVisibleApplications();
				
				if(manager.getForegroundProcessId() != lastProcessId)
				{
					lastProcessId = manager.getForegroundProcessId();
					
					for(int count = 0; visibleApplications.length > count; count++)
					{ 
						if(manager.getProcessId(visibleApplications[count]) == lastProcessId)
						{ 
							actLog.addAction(action.TYPE_APP,lastAppName+
									":"+(int)(new Date().getTime()-StartTimer.getTime())/1000);
							StartTimer = new Date();
							lastAppName = visibleApplications[count].getName();
							break;
						}
					}
				}
			}
	    }
        catch (InterruptedException e)
		{actLog.addAction(true,action.TYPE_APP,e.toString());}
        catch (Exception e)
		{actLog.addAction(true,action.TYPE_APP,e.toString());}
	}
}