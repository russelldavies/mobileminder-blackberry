package com.spotlight.track;

import java.util.Date;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;
  
import javax.microedition.io.Connector;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Persistable;


import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.sms.OutboundMessageListener;

import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;


/**
 * 
 * The Application Listener class stores the application currently running on the device at specific intervals in the action log in the form of an action of type app.
 *
 */

public class MyAppListener extends Thread
{
	private LocalDataAccess actLog;
	private int AppTimer;
	public MyAppListener(LocalDataAccess inputAccess, int inputAppTimer)
	{
		actLog = inputAccess;
		AppTimer = inputAppTimer;
		this.start();
	}

/**
* 
* 		Stores the Application currently running on the device.
* 
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
				this.sleep(AppTimer);
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