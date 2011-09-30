package com.mmtechco.mobileminder;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import net.rim.blackberry.api.sms.OutboundMessageListener;


//TODO find out what this implement.

/**
* 
* MyTextListener monitors and registers text message based events.
* 
*/
public class MyTextListener implements OutboundMessageListener, javax.wireless.messaging.MessageListener 
{
	private LocalDataAccess actLog;

/**
 * The constructor initialises the action store location and registers a MessageListener for the device.
 * 
 * @param inputAccess log of actions
 */
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
 * Adds a message and its address to the action log	
 * 
 * @param inputStatus message structure
 * @param inputDestinationAddress message address
 */
	
	  private void addToLog(String inputStatus,
							String inputDestinationAddress)
	  {
	    	actLog.addAction(action.TYPE_TEXT, inputStatus, inputDestinationAddress);//, inputDescriptor);
	  }
	  
/**
 * Adds an outgoing message to the action log
 * 
 * @param message message structure
 */

	public void notifyOutgoingMessage(Message message)
	{
		addToLog(action.Outgoing +" Message",message.getAddress());//,message.toString());
	}
	
/**
* Adds an incoming message to the action log
* 
* @param message message structure
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
