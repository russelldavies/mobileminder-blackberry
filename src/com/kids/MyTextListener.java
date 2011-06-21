package com.kids;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;

import net.rim.blackberry.api.sms.OutboundMessageListener;

/**
* 
* MyTextListener monitors and registers text message based events.
* 
*/
public class MyTextListener implements OutboundMessageListener, javax.wireless.messaging.MessageListener 
{
	private LocalDataReader actLog;
	public static Debug logWriter = Logger.getInstance();
	MessageConnection _mc;

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
           _mc = (MessageConnection)Connector.open("sms://:1212");   // is port 1212 valid??
           _mc.setMessageListener(this);
           //smsMessage.setMessage(_number, _outgoing, _inputBody)
        }
        catch (IOException e) 
        {
        	logWriter.log("MyTextListener::IOException:: "+e.getMessage());
        	//TODO: actLog.addMessage(smsMessage); 
        }
        catch (Exception e) 
        {
        	logWriter.log("MyTextListener::Exception:: "+e.getMessage());
        	//TODO:actLog.addMessage(smsMessage);
        }
	}
	
	
/**
 * Adds a message and its address to the action log	
 * 
 * @param inputStatus message structure
 * @param inputDestinationAddress message address
 * @param _date 
 * @param txtBody 
 */
	
	  private void addToLog(String inputStatus, String phNumber, String _date, String _txtBody)
	  {
		  logWriter.log("Adding to log:MyTextListener");
		  SMSMessage smsMessage=new SMSMessage();
		  boolean isOutgoing = false;
		  isOutgoing = inputStatus.equalsIgnoreCase("Outgoing Message") ? true : false;
		  
		  logWriter.log("inputStatus is: "+inputStatus);
		  logWriter.log("SMS Address: "+phNumber);
		  logWriter.log("Direction is: "+(isOutgoing ? "Outgoing":"Incoming"));
		  logWriter.log("Date: "+_date);
		  logWriter.log("SMS Body: "+_txtBody);
		  
		  smsMessage.setMessage(phNumber, isOutgoing, _date, _txtBody);

          logWriter.log("Adding message to log...");
          actLog.addMessage(smsMessage);
          logWriter.log("Message added to log...");
		  }

/**
 * Adds an outgoing message to the action log
 * 
 * @param message message structure
 */

	public void notifyOutgoingMessage(Message message)
	{
		logWriter.log("SMS message outgoing");

		// Get the body contents of the SMS Text
		String txtBody = "";
		// If "message" is an object of type "TextMessage"
		if ( message instanceof TextMessage )
		{
		    txtBody = ((TextMessage) message).getPayloadText();
		}
		
		// Add to log
		addToLog(action.Outgoing +" Message",message.getAddress(), Tools.getDate(), txtBody);
	}


	/**
	* Adds an incoming message to the action log
	* 
	* @param message message structure
	*/
	
	//In the simulator, ensure source and destination SMS ports are the same in the Network menu, 
	// This way, you will receive the text you just sent, and can test notifyIncomingMessage
		public void notifyIncomingMessage(MessageConnection conn) 
		{
			logWriter.log("SMS message incoming");
			
			// Get the message
			TextMessage message = null;
			try {
				message = (TextMessage) conn.receive();
			} catch (InterruptedIOException e1) {
				logWriter.log("MyTextListener::notifyIncomingMessage::InterruptedIOException::"+e1.getMessage());
				e1.printStackTrace();
			} catch (IOException e1) {
				logWriter.log("MyTextListener::notifyIncomingMessage::IOException::"+e1.getMessage());
				e1.printStackTrace();
			}
			addToLog(action.Incoming +" Message",message.getAddress(), Tools.getDate(),message.getPayloadText());
			
			
			//http://myhowto.org/java/j2me/22-sending-and-receiving-gsm-sms-on-blackberry-using-rim-apis/
			//http://www.blackberry.com/developers/docs/6.0.0api/Messaging-summary.html#MG_3
			/*
			Message m = null;
			try {
				m = _mc.receive();				
			} catch (InterruptedIOException e) {
				logWriter.log("MyTextListener::notifyIncomingMessage::InterruptedIOException::"+e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logWriter.log("MyTextListener::notifyIncomingMessage::IOException::"+e.getMessage());
				e.printStackTrace();
			} 
		    String address = m.getAddress(); 
		    String msg = null; 
		    if ( m instanceof TextMessage ) 
		    { 
		        TextMessage tm = (TextMessage)m; 
		        msg = tm.getPayloadText(); 
		    } 
		    else  if (m instanceof BinaryMessage) 
		    { 
		        byte[] data = ((BinaryMessage) m).getPayloadData(); 
		   
		        // convert Binary Data to Text 
		        try {
					msg = new String(data, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					logWriter.log("MyTextListener::notifyIncomingMessage::UnsupportedEncodingException::"+e.getMessage());
					e.printStackTrace();
				} 
		     } 
		     else 
		     {
		         logWriter.log("Invalid Message Format"); 
		         logWriter.log("Received SMS text from  " + address + " : " + msg); 
		     }
			*/


		}	

}