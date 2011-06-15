package com.kids;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;

import net.rim.blackberry.api.sms.OutboundMessageListener;


//TODO find out what this implement.

/**
* 
* MyTextListener monitors and registers text message based events.
* 
*/
public class MyTextListener implements OutboundMessageListener, javax.wireless.messaging.MessageListener 
{
	private LocalDataReader actLog;
	//private LocalDataReader actLog;
	Debug log = Logger.getInstance();
	//TODO:private SMSMessage smsMessage;

/**
 * The constructor initialises the action store location and registers a MessageListener for the device.
 * 
 * @param inputAccess log of actions
 */
	public MyTextListener(LocalDataReader inputAccess)
	{
		log.log("Starting MyTextListener...");
		actLog = inputAccess;
		
		try 
        {															
           MessageConnection _mc = (MessageConnection)Connector.open("sms://:0");
           _mc.setMessageListener(this);
           //smsMessage.setMessage(_number, _outgoing, _inputBody)
        }
        catch (IOException e) 
        {
        	//TODO: actLog.addMessage(smsMessage);
        }//true,action.TYPE_TEXT, e.toString());}
        catch (Exception e) 
        {
        	//TODO:actLog.addMessage(smsMessage);
        }//true,action.TYPE_TEXT, e.toString());}
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
		  log.log("Adding to log:MyTextListener");
	    	//TODO: actLog.addMessage(smsMessage);//(action.TYPE_TEXT, inputStatus, inputDestinationAddress);//, inputDescriptor);
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
		{
			//TODO:actLog.addMessage(smsMessage);
		}//(true,action.TYPE_TEXT,e.toString());} 
		catch (IOException e) 
		{
			//TODO:actLog.addMessage(smsMessage);
		}//(true,action.TYPE_TEXT,e.toString());}	
        catch (Exception e) 
        {
        	//TODO:actLog.addMessage(smsMessage);
        }//(true,action.TYPE_TEXT, e.toString());}
	}
}
