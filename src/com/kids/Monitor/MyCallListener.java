package com.kids.Monitor;

import java.util.Date;

import com.kids.Controllable;
import com.kids.Logger;
import com.kids.Data.Tools;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.enums.COMMAND_TARGETS;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;

/**
 * 
 * MyCallListener monitors and registers call based events.
 *
 */

public class MyCallListener extends AbstractPhoneListener implements Controllable 
{
        private LocalDataReader actLog;
        //private final String    Connected 		= "Connected";
        //private final String    Hold_ON   		= "Hold_ON";
        //private final String    Hold_OFF  		= "Hold_OFF";
        //private final String    Dial_OUT  		= "Dial_OUT";
        //private final String    Dial_IN   		= "Dial_IN";
        //private final String    Dropped   		= "Dropped";
        private final String    	Finished  		= "Finished";
        private       String    	Prefix          = "";
        private 	  String		contactName 	= "";
        private		  String		contactNumber	= "";
        private		  int 			callStartTime	= 0;
        private		  int 			callEndTime		= 0;
        private 	  boolean		isOutgoing		= false;
        			  Debug     	logWriter		= Logger.getInstance();
		private 	  MMTools 		tools 			= Tools.getInstance();
        private 	  CallMessage 	callMessage;



        /**
         * The constructor initialises the action store location and registers the callListener for the device.
         * 
         * @param inputAccess log of actions
         */
                
        public MyCallListener(LocalDataReader inputAccess)
        {
                logWriter.log("Start MyCallListener");
                callMessage = new CallMessage();
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
                        
            callMessage.clearData();
            logWriter.log("Setting CallMessage...");
            callMessage.setMessage( contactNumber,
                                    isOutgoing,
                                    tools.getDate(),
                                    (callEndTime-callStartTime)/1000
                                  );
            
            callMessage.setContactName( (null==contactName?"":contactName) );
            
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
            
            //contactName = PhoneCallLogID(Integer.parseInt(contactNumber)).getName();
            contactName = new PhoneCallLogID(contactNumber).getName();
            
            //contactName=callInfo.getDisplayPhoneNumber();
            
            logWriter.log("contactName="+contactName);
            
            if (null == contactName) contactName="";
        }

        /**
         * Adds action to log when the call has been directly connected
         * 
         * @param callId call address
         */     
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
        public void callHeld(int callId)
        { 
        	logWriter.log("MyCallListener::callHeld");
        	//addToLog(Prefix+Hold_ON, callId); 
        }
        
        /**
         * Adds action to log when the call has arrived
         * 
         * @param callId call address
         */
        public void callIncoming(int callId)
        {
        	logWriter.log("MyCallListener::callIncoming");
        	isOutgoing=false;
            Prefix = action.Incoming + " ";
            //addToLog(Dial_IN, callId); 
        }        
        
        /**
         * Adds action to log when the call has been initiated by the handheld
         * 
         * @param callId call address
         */
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

		public boolean isTarget(COMMAND_TARGETS inputCOMMAND_TARGETS)
		{
			logWriter.log("CallListener::isTarget::COMMAND_TARGETS");
			if(inputCOMMAND_TARGETS == COMMAND_TARGETS.CALL)
			{return true;} 
			else 
			{return false;}
		}

		public boolean isTarget(String inputCOMMAND_TARGETS) 
		{
			logWriter.log("CallListener::isTarget::String");
			if(inputCOMMAND_TARGETS.toString() == COMMAND_TARGETS.CALL.toString())
			{return true;} 
			else 
			{return false;}
		}

		public boolean processCommand(String[] arg0)
		{
			logWriter.log("CallListener::processCommand::String[]");
			// TODO Auto-generated method stub
			return false;
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