package com.kids;

import java.util.Date;

import javax.microedition.pim.Contact;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.AbstractPhoneListener;


/**
 * 
 * MyCallListener monitors and registers call based events.
 *
 */

public class MyCallListener extends AbstractPhoneListener
{
        private LocalDataWriter actLog;
        private final String    Connected 		= "Connected";
        private final String    Finished  		= "Finished";
        private final String    Hold_ON   		= "Hold_ON";
        private final String    Hold_OFF  		= "Hold_OFF";
        private final String    Dial_OUT  		= "Dial_OUT";
        private final String    Dial_IN   		= "Dial_IN";
        private final String    Dropped   		= "Dropped";
        private       String    Prefix          = "";
        private 	  String	contactName 	= "";
        private		  String	contactNumber	= "";
        private 	  boolean	isOutgoing		= false;
        private		  int 		callStartTime	= 0;
        private		  int 		callEndTime		= 0;
        //private                 Date          callStartTime
        //return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Debug logWriter = Logger.getInstance();

        /**
         * The constructor initialises the action store location and registers the callListener for the device.
         * 
         * @param inputAccess log of actions
         */
                
        public MyCallListener(LocalDataWriter inputAccess)
        {
                logWriter.log("Start MyCallListener");
                actLog = inputAccess;
                Phone.addPhoneListener(this);//TODO look up addPhonelistener
        }     
        
        /**
         * Stores call information to action log.
         * 
         * <p>
         * Retrieves the call information from the device and stores it in the action log as type Call
         * 
         * @param ehandler String consisting of a prefix and the status of the call i.e. connected
         *              
         * @param callid The address of the caller
         * 
         *
         */
        private void addToLog(String ehandler, int callId)
        {
            logWriter.log("In MyCallListener.addToLog");
            CallMessage callMessage=new CallMessage();
                        
            logWriter.log("Setting CallMessage...");
            callMessage.setMessage( contactNumber,
                                    isOutgoing,
                                    Tools.getDate(),
                                    (callEndTime-callStartTime)/1000
                                    );
            
            callMessage.setContactName(contactName);
            
            logWriter.log("Adding message to log...");
            actLog.addMessage(callMessage);
            logWriter.log("Message added to log..."); 
        }
        
/**
 * Adds action to log when the call has been connected
 * 
 * @param callId call address
 */
        public void callConnected(int callId)
        { 
        	logWriter.log("MyCallListener::callConnected");
        	callStartTime = (int) new Date().getTime();
            PhoneCall callInfo = Phone.getCall(callId);
            
            contactNumber=callInfo.getPhoneNumber();
            contactName=callInfo.getDisplayPhoneNumber();
            //BlackBerryContact contact = callInfo.getContact();
            //contactName = (contact == null ? null : contact.getString(BlackBerryContact.NAME, 0));//(Contact.NAME, 0);
            
            // We don't want the number to be the same as the contact name so
            logWriter.log("contactName="+contactName);
            if (contactNumber.equals(contactName))      //TODO: NullPointerException on call fail
            {
            	logWriter.log("Contact name=contact number");
                contactName="";
            }
        	//addToLog(Prefix+Connected, callId);
        }

        /**
         * Adds action to log when the call has been directly connected
         * 
         * @param callId call address
         */     

        // Direct-connect call connected
        public void callDirectConnectConnected(int callId)
        { 
        	logWriter.log("MyCallListener::callDirectConnectConnected");
        	//addToLog(Prefix+Connected, callId);
        }

        /**
         * Adds action to log when the call has been directly disconnected
         * 
         * @param callId call address
         */
        
        // Direct-connect call disconnected
        public void callDirectConnectDisconnected(int callId)
        { 
        	logWriter.log("MyCallListener::callDirectConnectDisconnected");
                if(false == Prefix.equals(""))
                {
                        //addToLog(Prefix+Finished, callId); 
                        Prefix = "";
                }
        }
        
        /**
         * Adds action to log when the call has been disconnected
         * 
         * @param callId call address
         */
        // Call disconnected
        public void callDisconnected(int callId)
        { 
        	callEndTime=(int) new Date().getTime();
        	logWriter.log("MyCallListener::callDisconnected");
                if(false == Prefix.equals(""))
                {
                        addToLog(Prefix+Finished, callId); 
                        Prefix = "";
                }
        }
        
        /**
         * Adds action to log when the call has been put on hold
         * 
         * @param callId call address
         */
        // Call has been placed on "hold"
        public void callHeld(int callId)
        { 
        	logWriter.log("MyCallListener::callHeld");
        	//addToLog(Prefix+Hold_ON, callId); 
        }//{ addToLog("callHeld", callId); }

        
        /**
         * Adds action to log when the call has arrived
         * 
         * @param callId call address
         */
        // New call has arrived
        public void callIncoming(int callId)
        {
        	logWriter.log("MyCallListener::callIncoming");
        	isOutgoing=false;
            Prefix = action.Incoming + " ";
            //addToLog(Dial_IN, callId); 
        }//{ addToLog("callIncoming", callId); }

        
        
        /**
         * Adds action to log when the call has been initiated by the handheld
         * 
         * @param callId call address
         */
        // Outbound call initiated by the handheld
        public void callInitiated(int callId)
        { 
        	logWriter.log("MyCallListener::callInitiated");
        	isOutgoing = true;
                Prefix = action.Outgoing + " ";
                //addToLog(Dial_OUT, callId); 
        }
        
        /**
         * Adds action to log when the call has been taken off hold
         * 
         * @param callId call address
         */
        // Call taken off of "hold"
        public void callResumed(int callId)
        { 
        	logWriter.log("MyCallListener::callResumed");
        	//addToLog(Prefix+Hold_OFF, callId); 
        }
        
        /**
         * Adds action to log when the call has failed
         * 
         * @param callId call address
         * @param reason failure reason
         */
        public void callFailed(int callId,int reason)
        {
        	logWriter.log("MyCallListener::callFailed");
        	//addToLog(Prefix+"Dropped", callId);
                Prefix = "";
            /*                    // determine reason
            switch(reason)//( error ) 
            {
              case PhoneListener.CALL_ERROR_AUTHORIZATION_FAILURE: break;
              case PhoneListener.CALL_ERROR_CALL_REPLACED_BY_STK: break;
              case PhoneListener.CALL_ERROR_CONGESTION: break;
              case PhoneListener.CALL_ERROR_CONNECTION_DENIED_BY_NETWORK: break;
              case PhoneListener.CALL_ERROR_DUE_TO_FADING: break;
              case PhoneListener.CALL_ERROR_EMERGENCY_CALLS_ONLY: break;
              case PhoneListener.CALL_ERROR_FDN_MISMATCH: break;
              case PhoneListener.CALL_ERROR_GENERAL: break;
              case PhoneListener.CALL_ERROR_HOLD_ERROR: break;
              case PhoneListener.CALL_ERROR_INCOMING_CALL_BARRED: break;
              case PhoneListener.CALL_ERROR_LOST_DUE_TO_FADING: break;
              case PhoneListener.CALL_ERROR_MAINTENANCE_REQUIRED: break;
              case PhoneListener.CALL_ERROR_NUMBER_NOT_IN_SERVICE: break;
              case PhoneListener.CALL_ERROR_NUMBER_UNOBTAINABLE: break;
              case PhoneListener.CALL_ERROR_OUTGOING_CALLS_BARRED: break;
              case PhoneListener.CALL_ERROR_PLEASE_TRY_LATER: break;
              case PhoneListener.CALL_ERROR_RADIO_PATH_UNAVAILABLE: break;
              case PhoneListener.CALL_ERROR_SERVICE_CONFLICT: break;
              case PhoneListener.CALL_ERROR_SERVICE_NOT_AVAILABLE: break;
              case PhoneListener.CALL_ERROR_SUBSCRIBER_BUSY: break;
              case PhoneListener.CALL_ERROR_SYSTEM_BUSY_TRY_LATER: break;
              case PhoneListener.CALL_ERROR_TRY_AGAIN: break;
              case PhoneListener.CALL_ERROR_USER_BUSY_IN_DATA: break;
              case PhoneListener.CALL_ERROR_USER_BUSY_IN_PRIVATE: break;
              case PhoneListener.CALL_ERROR_USER_NOT_AUTHORIZED: break;
              case PhoneListener.CALL_ERROR_USER_NOT_AVAILABLE: break;
              case PhoneListener.CALL_ERROR_USER_NOT_REACHABLE: break;
            }*/
        }
}

/**
 * This is an enumeration of the different ways in which a call can be ended.
 * Since enums don't exist in the blackberry API, a workaround has been implemented
 */

class CallEndStatus
{
        private CallEndStatus(){}
        //TODO: Implement these properly. ATM, we only set FINISH
        public static final byte OTHER     = 0;
        public static final byte NO_ANSWER = 1;
        public static final byte DROPPED   = 2;
        public static final byte FINISHED  = 3;
}