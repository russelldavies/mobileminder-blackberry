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
	public static Debug logWriter = Logger.getInstance();
	//private LocalDataReader actLog;
	//TODO:private SMSMessage smsMessage;

/**
 * The constructor initialises the action store location and registers a MessageListener for the device.
 * 
 * @param inputAccess log of actions
 */
	public MyTextListener(LocalDataReader inputAccess)
	{
		logWriter.log("Starting MyTextListener...");
		actLog = inputAccess;
		
		try 
        {															
           MessageConnection _mc = (MessageConnection)Connector.open("sms://:0");
           _mc.setMessageListener(this);
           //smsMessage.setMessage(_number, _outgoing, _inputBody)
        }
        catch (IOException e) 
        {
        	logWriter.log("MyTextListener::IOException:: "+e);
        	//TODO: actLog.addMessage(smsMessage); 
        }//true,action.TYPE_TEXT, e.toString());}
        catch (Exception e) 
        {
        	logWriter.log("MyTextListener::Exception:: "+e);
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
		  logWriter.log("Adding to log:MyTextListener");
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
			addToLog(action.Incoming+" Message",conn.receive().getAddress());
		}
		catch (InterruptedIOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//,conn.receive().toString());

	}
}
