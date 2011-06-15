package com.kids;
/*
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;
*/
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;

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
        private final String    Connected = "Connected";
        private final String    Finished  = "Finished";
        private final String    Hold_ON   = "Hold_ON";
        private final String    Hold_OFF  = "Hold_OFF";
        private final String    Dial_OUT  = "Dial_OUT";
        private final String    Dial_IN   = "Dial_IN";
        private final String    Droped    = "Droped";
        private           String    Prefix    = "";
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
         * Returns the phone number of the caller       
         * 
         * @param  callId integer representing the phone call id
         * @return A string is returned containing the callers phone number
         */
        //TODO what is this function used for? Deosn't seem to be used anywhere in the code
        private String getPhoneNumber(int callId)
        {
                logWriter.log("getPhoneNumber");
                return Phone.getCall(callId).getDisplayPhoneNumber();
        }
        /*
        private void addTimedEntryToLog(String ehandler, int callId, startTime, endTime)
        {
                
        }
        */
        
        
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
                logWriter.log("Adding message to log...");
                logWriter.log("callID="+callId);
            PhoneCall callInfo = Phone.getCall(callId);
            CallMessage callMessage=new CallMessage();
            String contactNumber=callInfo.getPhoneNumber().toString(); // TODO: Causing NullPointerException
            String contactName=callInfo.getDisplayPhoneNumber();
                    
            if(null != callInfo)
            {
                    
                    callMessage.setMessage( contactNumber,
                                                                callInfo.isOutgoing(),
                                                                Tools.getDate(),
                                                                callInfo.getElapsedTime());
                //actLog.addMessage(true,action.TYPE_CALL, ehandler);
                    
                    // Store the contact name
                    if (contactNumber.equals(contactName))      //TODO: NullPointerException on call fail
                        contactName="";
                    
                    callMessage.setContactName(contactName);                
                actLog.addMessage(callMessage);
            }/*
            else
            {
                // If, for some reason, the call(callId) doesnt exist (when??), then set a basic blank message
                callMessage.setMessage("", false, Tools.getDate(), 0);
                //callMessage.setMessage(callInfo.getPhoneNumber(), callInfo.isOutgoing());
                actLog.addMessage(callMessage);
                
                
                //Original code
                //actLog.addMessage(action.TYPE_CALL, ehandler, callInfo.getDisplayPhoneNumber());//, callInfo.getStatusString());
            }*/
        }

        // A call has been added to a conference call
        //public void callAdded(int callId)
        //{ addToLog("callAdded", callId); }
        
        // User answered a call
        //public void callAnswered(int callId)//call is incoming but user is still to pickup
        //{ addToLog(Prefix+Connected+1, callId); }
        
        // Conference call established
        //public void callConferenceCallEstablished(int callId)
        //{ addToLog("callConferenceCallEstablished", callId); }
        
        // Network indicates a connected event
        
/**
 * Adds action to log when the call has been connected
 * 
 * @param callId call address
 */
        public void callConnected(int callId)
        { addToLog(Prefix+Connected, callId); }//{ addToLog("callConnected", callId); }

        /**
         * Adds action to log when the call has been directly connected
         * 
         * @param callId call address
         */     

        // Direct-connect call connected
        public void callDirectConnectConnected(int callId)
        { addToLog(Prefix+Connected, callId); }//{ addToLog("callDirectConnectConnected", callId); }

        /**
         * Adds action to log when the call has been directly disconnected
         * 
         * @param callId call address
         */
        
        // Direct-connect call disconnected
        public void callDirectConnectDisconnected(int callId)
        { 
                if(false == Prefix.equals(""))
                {
                        addToLog(Prefix+Finished, callId); 
                        Prefix = "";
                }
        }//{ addToLog("callDirectConnectDisconnected", callId); }

        
        /**
         * Adds action to log when the call has been disconnected
         * 
         * @param callId call address
         */
        // Call disconnected
        public void callDisconnected(int callId)
        { 
                if(false == Prefix.equals(""))
                {
                        addToLog(Prefix+Finished, callId); 
                        Prefix = "";
                }
        }//{ addToLog("callDisconnected", callId); }
        
        // User ended call.. this will be called as well as "callDisconnected(int)"
        //public void callEndedByUser(int callId)
        //{ addToLog("Finished", callId); }//{ addToLog("callEndedByUser", callId); }
        
        /**
         * Adds action to log when the call has been put on hold
         * 
         * @param callId call address
         */
        // Call has been placed on "hold"
        public void callHeld(int callId)
        { addToLog(Prefix+Hold_ON, callId); }//{ addToLog("callHeld", callId); }

        
        /**
         * Adds action to log when the call has arrived
         * 
         * @param callId call address
         */
        // New call has arrived
        public void callIncoming(int callId)
        {
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
                Prefix = action.Outgoing + " ";
                //addToLog(Dial_OUT, callId); 
        }//{ addToLog("callInitiated", callId); }
        
        // Call removed from a conference call
        //public void callRemoved(int callId)
        //{ addToLog("callRemoved", callId); }

        
        
        /**
         * Adds action to log when the call has been taken off hold
         * 
         * @param callId call address
         */
        // Call taken off of "hold"
        public void callResumed(int callId)
        { addToLog(Prefix+Hold_OFF, callId); }//{ addToLog("callResumed", callId); }
        
        // Call is waiting
        //public void callWaiting(int callid)
        //{ addToLog("callWaiting", callid); }
        
        // Conference call has been terminated
        // (all members disconnected)
        //public void conferenceCallDisconnected(int callId)
        //{ addToLog("conferenceCallDisconnected", callId); }

        
        
        /**
         * Adds action to log when the call has failed
         * 
         * @param callId call address
         * @param reason failure reason
         */
        public void callFailed(int callId,int reason)
        {
                //actLog.addAction(true,action.TYPE_CALL,"callFailed:"+reason);
                addToLog(Prefix+"Dropped", callId);
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
        
        public static final byte OTHER     = 0;
        public static final byte NO_ANSWER = 1;
        public static final byte DROPPED   = 2;
        public static final byte FINISHED  = 3;
}



/*
class CallEndSataus {
    public static CallEndSataus OTHER = new CallEndSataus(0);
    public static CallEndSataus NO_ANSWER = new CallEndSataus(1);
    public static CallEndSataus DROPPED = new CallEndSataus(2);
    public static CallEndSataus FINISHED = new CallEndSataus(3);
    
    private int _value;

    private CallEndSataus(int value) {
        _value = value;
    }
        
    public int toInt() {
        return this._value;
    }
    public static CallEndSataus fromInt(int value) {
        switch(value)
        {
                case 0: return OTHER;
                case 1: return NO_ANSWER;
                case 2: return DROPPED;
                case 3: return FINISHED;
        }
                //return DROPPED;
    }
}*/
