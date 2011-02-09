/**
 * 
 * MyServerUpload.java - A class that 
 * 
 *
 */

package com.spotlight.track;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Date;
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

public class MyServerUpload extends Thread
{
	private LocalDataAccess actLog;
	private int             sendToServerTime;
	private int 			deviceID;
	private int				employerID;
	
	public MyServerUpload(LocalDataAccess inputAccess,
						  int			  inputEmployerID,
						  int 			  inputDeviceID,
						  int			  inputUploadTimer)
	{
		actLog           = inputAccess;
		employerID 		 = inputEmployerID;
		deviceID 		 = inputDeviceID;
		sendToServerTime = inputUploadTimer;
				
		this.start();
	}

	
/** 
 * 
 * @brief
 * 		Sends all device actions to the server within specific intervals
 * 
 * @details
 *  	This function is called when the constructor starts, it stores the actions to the action log.
 *  	It sleeps for a certain interval, then sends a batch to the server of all the actions during the interval.
 *  	Each action is loaded, sent and then removed and the same process is used for the next action.
 *  	If an error occurs a error message is logged in the action log.
 * 	
 */
	
	public void run()
	{
		actLog.addAction(action.TYPE_SERVER,"Starting: MyServerUpload");
		
		try
		{
			SLDBserviceSoap_Stub server = new SLDBserviceSoap_Stub();
			
			while(true)
			{
				this.sleep(sendToServerTime);

				while(0 != actLog.length())//Dont send Start Upload
				{
					
					action anAction = actLog.getAction(0);
					String result = server.addAction(
										employerID,
										deviceID,
										anAction.getIsError(),
										anAction.getType(),
										anAction.getTimeStamp(),
										anAction.getStatus(),
										anAction.getDestinationAddress());
	
					if("ok".equals(result))
					{
						actLog.removeAction(0);//remove the First Action
						System.err.println(result);
					}
					else
					{
						actLog.addAction(true, action.TYPE_SERVER,"Error uploading Action to server: "+result);//.replace('\n', ' '));
						System.err.println(result);
						break;//jump out of loop!
					}
				}

			}
		} 
		catch(InterruptedException e)
		{actLog.addAction(true,action.TYPE_SERVER,e.toString());}
		catch(Exception e)
		{actLog.addAction(true,action.TYPE_SERVER,e.toString());}
    }
}

