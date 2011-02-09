/**
 * 
 * Call listener class
 * 
 *
 */

package com.spotlight.track;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Date;
import java.util.Vector;
  
import javax.microedition.io.Connector;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Persistable;


import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.sms.OutboundMessageListener;

import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;


//TODO look up abstractphonelistener
public class MyCallListener extends AbstractPhoneListener
{
	private LocalDataAccess actLog;
	private final String    Connected = "Connected";
	private final String    Finished  = "Finished";
	private final String	Hold_ON   = "Hold_ON";
	private final String	Hold_OFF  = "Hold_OFF";
	private final String	Dial_OUT  = "Dial_OUT";
	private final String	Dial_IN   = "Dial_IN";
	private final String	Droped    = "Droped";
	private 	  String    Prefix    = "";
	//private		  Date		callStartTime
	//return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	
	public MyCallListener(LocalDataAccess inputAccess)
	{
		actLog = inputAccess;
		Phone.addPhoneListener(this);//TODO look up addPhonelistener
	}
	
/**
 * @brief
 * 		returns the phone number of the caller	
 * @param [in] callId <integer representing the phone call id>
 * @return String is returned containing the callers phone number
 */
	//TODO what is this function used for? Deosn't seem to be used anywhere in the code
	private String getPhoneNumber(int callId)
	{
		return Phone.getCall(callId).getDisplayPhoneNumber();
	}
	/*
	private void addTimedEntryToLog(String ehandler, int callId, startTime, endTime)
	{
		
	}
	*/
	
	
/**
 * 
 * @brief
 * 		Stores call information to action log
 * 
 * @detail
 * 		retrieves the call information from the device and stores it in the action log as type Call
 * 
 * @param [in] ehandler <String consisting of a prefix and the status of the call i.e. connected>
 * 		
 * @param [in] callid <
 * 
 *
 */
	private void addToLog(String ehandler, int callId)
	{
		
	    PhoneCall callInfo = Phone.getCall(callId);
	    
	    if(null == callInfo)
	    {
	    	actLog.addAction(true,action.TYPE_CALL, ehandler);
	    }
	    else
	    {
	    	actLog.addAction(action.TYPE_CALL, ehandler, callInfo.getDisplayPhoneNumber());//, callInfo.getStatusString());
	    }
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
	public void callConnected(int callId)
	{ addToLog(Prefix+Connected, callId); }//{ addToLog("callConnected", callId); }
	
	// Direct-connect call connected
	public void callDirectConnectConnected(int callId)
	{ addToLog(Prefix+Connected, callId); }//{ addToLog("callDirectConnectConnected", callId); }
	
	// Direct-connect call disconnected
	public void callDirectConnectDisconnected(int callId)
	{ 
		if(false == Prefix.equals(""))
		{
			addToLog(Prefix+Finished, callId); 
			Prefix = "";
		}
	}//{ addToLog("callDirectConnectDisconnected", callId); }
	
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

	// Call has been placed on "hold"
	public void callHeld(int callId)
	{ addToLog(Prefix+Hold_ON, callId); }//{ addToLog("callHeld", callId); }
	
	// New call has arrived
	public void callIncoming(int callId)
	{
		Prefix = action.Incoming + " ";
		//addToLog(Dial_IN, callId); 
	}//{ addToLog("callIncoming", callId); }
	
	// Outbound call initiated by the handheld
	public void callInitiated(int callId)
	{ 
		Prefix = action.Outgoing + " ";
		//addToLog(Dial_OUT, callId); 
	}//{ addToLog("callInitiated", callId); }
	
	// Call removed from a conference call
	//public void callRemoved(int callId)
	//{ addToLog("callRemoved", callId); }
	
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
	
	public void callFailed(int callId,int reason)
	{
		//actLog.addAction(true,action.TYPE_CALL,"callFailed:"+reason);
		addToLog(Prefix+"Droped", callId);
		Prefix = "";
	    /*			  // determine reason
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
