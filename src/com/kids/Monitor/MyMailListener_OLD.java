package com.kids.Monitor;

import com.kids.Logger;
import com.kids.Data.Tools;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;

import net.rim.blackberry.api.mail.Message;
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
public class MyMailListener_OLD implements SendListener//,*/ FolderListener, StoreListener
{
	private LocalDataReader actLog;
			Debug			log			= Logger.getInstance();
			MailMessage		mailMessage;

/**
 * The constructor initialises the action store location 
 * and registers a SendListener for the device.
 * 
 * @param inputAccess log of actions
 */
	public MyMailListener_OLD(LocalDataReader inputAccess)
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
		{
			log.log("x::MyMailListener::NoSuchServiceException::"+e.getMessage());
			//actLog.addMessage(true,action.TYPE_MAIL,e.toString());
		}
		catch(Exception e) 
		{
			log.log("x::MyMailListener::Exception::"+e.getMessage());
			//actLog.addMessage(true,action.TYPE_MAIL,e.toString());
		}
	}

/**
 * Retrieves and formats e-mail data and adds it to the action log
 * 
 * @param arg0 device e-mail data
 * @return boolean error value
 */
	
	public boolean addToLog(Message arg0)
	{
		StringBuffer DestinationAddress = new StringBuffer();
		MailMessage mailMessage = new MailMessage();
		
		try
		{
			DestinationAddress.append(arg0.getFolder().getStore().getServiceConfiguration().getEmailAddress());
			
			Address DestinationAddressArray[] = arg0.getRecipients(arg0.getMessageType());
			
			for(int count = 0; count<DestinationAddressArray.length; count++)
			{	//Format e-mail addresses: place comma between each address
				DestinationAddress.append(Tools.RestElementSeparator);
				DestinationAddress.append(DestinationAddressArray[count].getAddr());
			}
		} 
		catch (MessagingException e)
		{
			actLog.addMessage(mailMessage);	//TODO: Double check this is what we want to do, log instead?
			//actLog.addMessage(true,action.TYPE_MAIL,e.toString());
		}
		catch (Exception e)
		{
			actLog.addMessage(mailMessage);	//TODO: Double check this is what we want to do, log instead?
			//actLog.addMessage(true,action.TYPE_MAIL,e.toString());
		}

		//actLog.addMessage(action.TYPE_MAIL, arg0.getSubject(),DestinationAddress.toString());
	
		return false;
	}

	public boolean sendMessage(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

} // End class MyMailListener

class MailDirectionStatus
{
	private MailDirectionStatus(){}
	
	public static final byte INBOUND  = 0;
	public static final byte OUTBOUND = 1;
}