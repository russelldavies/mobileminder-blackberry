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
	
	  private void addToLog(String inputStatus, String inputDestinationAddress, Date _date, String _txtBody)
	  {
		  logWriter.log("Adding to log:MyTextListener");
		  logWriter.log("inputStatus is: "+inputStatus);
		  logWriter.log("inputDestinationAddress is: "+inputDestinationAddress);
		  SMSMessage smsMessage=new SMSMessage();
		  boolean isOutgoing = false;
		  isOutgoing = inputStatus.equalsIgnoreCase("Outgoing Message") ? true : false;
		  
		  logWriter.log("SMS Address: "+inputDestinationAddress);
		  logWriter.log("Direction is: "+(isOutgoing ? "Outgoing":"Incoming"));
		  logWriter.log("Date: "+_date);
		  logWriter.log("SMS Body: "+_txtBody);
		  
		  smsMessage.setMessage(inputDestinationAddress, isOutgoing, _date, _txtBody);

		  // TODO: Implement addMessage for SMS
		  actLog.addMessage(smsMessage);//(action.TYPE_TEXT, inputStatus, inputDestinationAddress);//, inputDescriptor);
	  }

/**
 * Adds an outgoing message to the action log
 * 
 * @param message message structure
 */

	public void notifyOutgoingMessage(Message message)
	{
		logWriter.log("SMS message outgoing");
		TextMessage txtMessage=null;
		try {
			txtMessage = (TextMessage) _mc.receive();
		} catch (InterruptedIOException e) {
			logWriter.log("MyTextListener::notifyOutgoingMessage::InterruptedIOException::"+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logWriter.log("MyTextListener::notifyOutgoingMessage::IOException::"+e.getMessage());
			e.printStackTrace();
		}
		addToLog(action.Outgoing +" Message",message.getAddress(), message.getTimestamp(),txtMessage.getPayloadText());
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
			addToLog(action.Outgoing +" Message",message.getAddress(), message.getTimestamp(),message.getPayloadText());
	
			
			
			
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
			/*
			try
			{
				addToLog(action.Incoming+" Message",conn.receive().getAddress(),msg.getTimestamp());
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
			*/

		}	

}