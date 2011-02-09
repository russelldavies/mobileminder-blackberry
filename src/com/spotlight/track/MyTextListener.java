/**
 * 
 * Text listener class
 * 
 *
 */

package com.spotlight.track;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import java.util.Date;
import java.util.Vector;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;


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


//TODO find out what this implement.
public class MyTextListener implements OutboundMessageListener, javax.wireless.messaging.MessageListener 
{
	private LocalDataAccess actLog;
	
	public MyTextListener(LocalDataAccess inputAccess)
	{
		actLog = inputAccess;
		
		try 
        {															
           MessageConnection _mc = (MessageConnection)Connector.open("sms://:0");
           _mc.setMessageListener(this);
        }
        catch (IOException e) 
        {actLog.addAction(true,action.TYPE_TEXT, e.toString());}
        catch (Exception e) 
        {actLog.addAction(true,action.TYPE_TEXT, e.toString());}
	}
	
	
/**
 * @brief
 * 		Adds a message and its address to the action log	
 * 
 * @param [in] inputStatus message structure
 * @param [in] inputDestinationAddress message address
 */
	
	  private void addToLog(String inputStatus,
							String inputDestinationAddress)
	  {
	    	actLog.addAction(action.TYPE_TEXT, inputStatus, inputDestinationAddress);//, inputDescriptor);
	  }
	  
/**
 * 
 * @brief
 * 		Adds an outgoing message to the action log
 * 
 * @param [in] message <message structure>
 */

	public void notifyOutgoingMessage(Message message)
	{
		addToLog(action.Outgoing +" Message",message.getAddress());//,message.toString());
	}
	
/**
* 
* @brief
* 		Adds an incoming message to the action log
* 
* @param [in] message <message structure>
*/
	public void notifyIncomingMessage(MessageConnection conn) 
	{
		try 
		{
			addToLog(action.Incoming+" Message",conn.receive().getAddress());//,conn.receive().toString());
		} 
		catch (InterruptedIOException e) 
		{actLog.addAction(true,action.TYPE_TEXT,e.toString());} 
		catch (IOException e) 
		{actLog.addAction(true,action.TYPE_TEXT,e.toString());}	
        catch (Exception e) 
        {actLog.addAction(true,action.TYPE_TEXT, e.toString());}
	}
}
