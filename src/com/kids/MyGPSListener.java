package com.kids;


import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Date;
import java.util.Vector;
  
import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import com.kids.prototypes.LocalDataWriter;

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
 * MyGPSListener monitors and registers location based events.
 *
 */

public class MyGPSListener extends Thread
{
	private LocalDataWriter actLog;
	private int 			timer;

/**
 * The GPSListener constructor initialise the action store location and the interval value.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputGPSTimer interval value
 */

public MyGPSListener(LocalDataWriter inputAccess, int inputGPSTimer)
	{
		actLog = inputAccess;
		timer  = inputGPSTimer;
		this.start();
	}
	
/**
 * When this function is started it poles the GPS service based on the interval specified in the constructor.
 * When the location changes this recorded in local data access.  
* 		Stores GPS location information about the device in the form of an action to the action log
* <p>   Its sets the criteria for the type of location information needed.
* 		It retrieves the location and saves it to the action log.
*
*/	
	
	
	public void run()
	{/*
		try
	    {
			while(true)
			{
				this.sleep(timer);
				
				Criteria criteria = new Criteria();
				criteria.setHorizontalAccuracy(       Criteria.NO_REQUIREMENT);
		        criteria.setVerticalAccuracy(         Criteria.NO_REQUIREMENT);
		        criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
		        criteria.setCostAllowed(true);
		        
				Location mylocation = LocationProvider.getInstance(criteria).getLocation(3);//Throws: NullPointerException
				//If there has been a change in the location of the device it is stored to the action log as an action of type GPS.
				if(mylocation.getSpeed()>0)
				{
					float  heading   = mylocation.getCourse();
		            double longitude = mylocation.getQualifiedCoordinates().getLongitude();
		            double latitude  = mylocation.getQualifiedCoordinates().getLatitude();
		            float  altitude  = mylocation.getQualifiedCoordinates().getAltitude();
		            float  speed     = mylocation.getSpeed();
					
					actLog.addAction(action.TYPE_GPS,
									 "Course:"+heading+" Speed:"+speed,
									 "lon:"+longitude+" lat:"+latitude);
				}
			}
		} 
        catch (LocationException e)
        {actLog.addAction(true,action.TYPE_GPS,e.toString());}
        catch (InterruptedException e)
		{actLog.addAction(true,action.TYPE_GPS,e.toString());}
        catch (Exception e)
		{actLog.addAction(true,action.TYPE_GPS,e.toString());}
*/
	}	// end run()
}

