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
 

class Controller extends Application
{	
    public static void main(String[] args)
    {
    	new Controller().enterEventDispatcher();
    }
     
    public Controller()
    {
    	int employerID  = 1;
    	int deviceID    = 2;
    	int oneSec	  = 1000;
    	int uploadTimer =  1*oneSec;//send update every
    	int GPSTimer    = 15*oneSec;//check GPS every
    	int AppTimer    =  2*oneSec;//check running app every
    	
    	LocalDataAccess actLog = new LocalDataAccess();
    	actLog.addAction(action.TYPE_IDEL,"Starting Controler");
    	
    	/*synchronized(Application.getEventLock()){    UiEngine ui = Ui.getUiEngine();
        Screen screen = new Dialog(Dialog.D_OK, "Shirts!!!!!!",
            Dialog.OK,           Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),         Manager.VERTICAL_SCROLL);
        ui.pushGlobalScreen(screen, 1, UiEngine.GLOBAL_QUEUE);
    }*/
    	//actLog.removeAction(0);
    	new MyCallListener(actLog);
    	new MyTextListener(actLog);
    	new MyMailListener(actLog);
    	new MyGPSListener (actLog, GPSTimer);
    	new MyAppListener (actLog, AppTimer);
    	new MyServerUpload(actLog, employerID, deviceID, uploadTimer);
    }
    
}

class MyServerUpload extends Thread
{
	private LocalDataAccess actLog;
	private int             sendToServerTime;
	private int 			deviceID;
	private int				employerID;
	
	public MyServerUpload(LocalDataAccess inputAccess,
						  int			  inputEmployerID,
						  int 			  inputDeviceID,
						  int			  inputUploadTimer)
	{
		actLog           = inputAccess;
		employerID 		 = inputEmployerID;
		deviceID 		 = inputDeviceID;
		sendToServerTime = inputUploadTimer;
				
		this.start();
	}
	
	public void run()
	{
		actLog.addAction(action.TYPE_SERVER,"Starting: MyServerUpload");
		
		try
		{
			SLDBserviceSoap_Stub server = new SLDBserviceSoap_Stub();
			
			while(true)
			{
				this.sleep(sendToServerTime);

				while(0 != actLog.length())//Dont send Start Upload
				{
					
					action anAction = actLog.getAction(0);
					String result = server.addAction(
										employerID,
										deviceID,
										anAction.getIsError(),
										anAction.getType(),
										anAction.getTimeStamp(),
										anAction.getStatus(),
										anAction.getDestinationAddress());
	
					if("ok".equals(result))
					{
						actLog.removeAction(0);//remove the First Action
						System.err.println(result);
					}
					else
					{
						actLog.addAction(true, action.TYPE_SERVER,"Error uploading Action to server: "+result);//.replace('\n', ' '));
						System.err.println(result);
						break;//jump out of loop!
					}
				}

			}
		} 
		catch(InterruptedException e)
		{actLog.addAction(true,action.TYPE_SERVER,e.toString());}
		catch(Exception e)
		{actLog.addAction(true,action.TYPE_SERVER,e.toString());}
    }
}

class MyAppListener extends Thread
{
	private LocalDataAccess actLog;
	private int AppTimer;
	public MyAppListener(LocalDataAccess inputAccess, int inputAppTimer)
	{
		actLog = inputAccess;
		AppTimer = inputAppTimer;
		this.start();
	}
	
	public void run()
	{
		try
	    {
			int lastProcessId = 0;
			ApplicationManager manager = ApplicationManager.getApplicationManager();
			Date StartTimer  = new Date();
			String lastAppName = "BootUp Device";
			
			while(true)
			{
				this.sleep(AppTimer);
				ApplicationDescriptor visibleApplications[] = manager.getVisibleApplications();
				
				if(manager.getForegroundProcessId() != lastProcessId)
				{
					lastProcessId = manager.getForegroundProcessId();
					
					for(int count = 0; visibleApplications.length > count; count++)
					{ 
						if(manager.getProcessId(visibleApplications[count]) == lastProcessId)
						{ 
							actLog.addAction(action.TYPE_APP,lastAppName+
									":"+(int)(new Date().getTime()-StartTimer.getTime())/1000);
							StartTimer = new Date();
							lastAppName = visibleApplications[count].getName();
							break;
						}
					}
				}
			}
	    }
        catch (InterruptedException e)
		{actLog.addAction(true,action.TYPE_APP,e.toString());}
        catch (Exception e)
		{actLog.addAction(true,action.TYPE_APP,e.toString());}
	}
}

class MyGPSListener extends Thread
{
	private LocalDataAccess actLog;
	private int 			timer;
	
	public MyGPSListener(LocalDataAccess inputAccess, int inputGPSTimer)
	{
		actLog = inputAccess;
		timer  = inputGPSTimer;
		this.start();
	}
	
	public void run()
	{
		try
	    {
			while(true)
			{
				this.sleep(timer);
				
				Criteria criteria = new Criteria();
				criteria.setHorizontalAccuracy(       Criteria.NO_REQUIREMENT);
		        criteria.setVerticalAccuracy(         Criteria.NO_REQUIREMENT);
		        criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
		        criteria.setCostAllowed(true);
		        
				Location mylocation = LocationProvider.getInstance(criteria).getLocation(3);//Throws: NullPointerException
				
				if(mylocation.getSpeed()>0)// a bit of a hack :p
				{
					float  heading   = mylocation.getCourse();
		            double longitude = mylocation.getQualifiedCoordinates().getLongitude();
		            double latitude  = mylocation.getQualifiedCoordinates().getLatitude();
		            float  altitude  = mylocation.getQualifiedCoordinates().getAltitude();
		            float  speed     = mylocation.getSpeed();
					
					actLog.addAction(action.TYPE_GPS,
									 "Course:"+heading+" Speed:"+speed,
									 "lon:"+longitude+" lat:"+latitude);
				}
			}
		} 
        catch (LocationException e)
        {actLog.addAction(true,action.TYPE_GPS,e.toString());}
        catch (InterruptedException e)
		{actLog.addAction(true,action.TYPE_GPS,e.toString());}
        catch (Exception e)
		{actLog.addAction(true,action.TYPE_GPS,e.toString());}

	}
}

class MyMailListener implements SendListener
{
	private LocalDataAccess actLog;
	
	public MyMailListener(LocalDataAccess inputAccess)
	{
		actLog = inputAccess;
		//MailSendListener mailSL = new mailSendListener();

		try
		{
			Store mailStore = Session.waitForDefaultSession().getStore();
			mailStore.addSendListener(this);
		}
		catch(NoSuchServiceException e) 
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}
		catch(Exception e) 
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}
	}

	public boolean sendMessage(net.rim.blackberry.api.mail.Message arg0) 
	{
		StringBuffer DestinationAddress = new StringBuffer();
		
		try
		{
			DestinationAddress.append(arg0.getFolder().getStore().getServiceConfiguration().getEmailAddress());
			
			Address DestinationAddressArray[] = arg0.getRecipients(arg0.getMessageType());
			
			for(int count = 0; count<DestinationAddressArray.length; count++)
			{
				DestinationAddress.append(", ");
				DestinationAddress.append(DestinationAddressArray[count].getAddr());
			}
		} 
		catch (MessagingException e)
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}
		catch (Exception e)
		{actLog.addAction(true,action.TYPE_MAIL,e.toString());}

		actLog.addAction(action.TYPE_MAIL, arg0.getSubject(),
						DestinationAddress.toString());//Integer.toString(arg0.getStatus())
	
		return false;
	}
}

class MyTextListener implements OutboundMessageListener, javax.wireless.messaging.MessageListener
{
	private LocalDataAccess actLog;
	
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
	
	  private void addToLog(String inputStatus,
							String inputDestinationAddress)
	  {
	    	actLog.addAction(action.TYPE_TEXT, inputStatus, inputDestinationAddress);//, inputDescriptor);
	  }
	  
	public void notifyOutgoingMessage(Message message) 
	{
		addToLog(action.Outgoing +" Message",message.getAddress());//,message.toString());
	}

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

class MyCallListener extends AbstractPhoneListener
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
		Phone.addPhoneListener(this);
	}
	
	private String getPhoneNumber(int callId)
	{
		return Phone.getCall(callId).getDisplayPhoneNumber();
	}
	/*
	private void addTimedEntryToLog(String ehandler, int callId, startTime, endTime)
	{
		
	}
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

class LocalDataAccess
{	
	private PersistentObject store;//HD
	private Vector LocalData;      //list of Actions
	
	public LocalDataAccess()
	{
		setupStore();
	}
	
	public String getDate()
	{
		//return new SimpleDateFormat("HH:mm:ss dd-MM-yy").format(new Date());
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}
	
	private void setupStore()
	{
		store = PersistentStore.getPersistentObject(0xdec6a67096f833cL);
		
		synchronized(store)
		{
			if(null == store.getContents()) 
			{
				store.setContents(new Vector());
				store.commit();
			}
		}
		
		LocalData = new Vector();
		LocalData = (Vector)store.getContents();
	}
	
	public void addAction(int inputType, String inputStatus)
	{
		addAction(false, inputType,inputStatus);
	}
	
	public void addAction(boolean inputError, int inputType, String inputStatus)
	{
		addAction(inputError, inputType,inputStatus,"");
	}
	
	public void addAction(int  inputType, 
						  String inputStatus,
						  String inputDestinationAddress)
	{
		addAction(false,inputType,inputStatus,inputDestinationAddress);
	}
	public void addAction(boolean error,
						  int    inputType, 
						  String  inputStatus,
					 	  String  inputDestinationAddress)
	{
		System.err.println("----------------"+inputDestinationAddress);
	/*	
		synchronized(Application.getEventLock()){    UiEngine ui = Ui.getUiEngine();
        Screen screen = new Dialog(Dialog.D_OK, "PantaLOOONS!!!!!!",
            Dialog.OK,           Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),         Manager.VERTICAL_SCROLL);
        ui.pushGlobalScreen(screen, 1, UiEngine.GLOBAL_QUEUE);
    }*/
		
		synchronized(store) 
		{
			LocalData.addElement(new action(error,
										    inputType, 
											getDate(), 
											inputStatus,
											inputDestinationAddress));
			store.setContents(LocalData);
			store.commit();
		}
	}
	
	public action getAction(int index)
	{
		synchronized (store)
		{
			LocalData = (Vector)store.getContents();
			
			if (!LocalData.isEmpty()) 
			{
				return (action)LocalData.elementAt(index);
			}
			else
			{
				return new action();
			}
		}
	}
		
	public boolean removeAction(int index)
	{
		synchronized (store)
		{
			LocalData = (Vector)store.getContents();
			
			if (!LocalData.isEmpty()) 
			{
				if(LocalData.size() > index)//test if the index is there.
				{
					LocalData.removeElementAt(index);
					return true;
				}
				else
				{return false;}
			}
			else
			{return false;}
		}
	}
	
	public int length()
	{
		synchronized (store)
		{
			LocalData = (Vector)store.getContents();
			return LocalData.size();
		}
	}
}
/*
class toast implements Runnable
{
	
	private String Message;
	
	toast(String inputMessage)
	{
		Message = inputMessage;
	}
	
    public void run() 
    {
    	Status.show(Message);
    }
}
*/
class action implements Persistable
{ 
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_IDEL    = 1;
	public static final int TYPE_CALL    = 2;
	public static final int TYPE_TEXT    = 3;
	public static final int TYPE_MAIL    = 4;
	public static final int TYPE_WEB     = 5;
	public static final int TYPE_APP     = 6;
	public static final int TYPE_GPS     = 7;
	public static final int TYPE_SERVER  = 8;
	
	public static final String Outgoing = "Outgoing";
	public static final String Incoming = "Incoming";
	
	private int     type;
	private boolean error;
	private String  timeStamp;
	private String  destinationAddress;
	private String  status;

	
	public action()
	{
		this(false,action.TYPE_UNKNOWN,"","","");
	}
	
	public action(int    inputError,
				  int    inputType, 
				  String inputTimeStamp, 
				  String inputStatus,
				  String inputDestinationAddress)
	{
		this(((0==inputError)?Boolean.FALSE:Boolean.TRUE).booleanValue(),
				inputType,
				inputTimeStamp,
				inputStatus,
				inputDestinationAddress);
	}
	
	public action(boolean inputError,
				  int    inputType, 
				  String  inputTimeStamp, 
				  String  inputStatus,
				  String  inputDestinationAddress)
	{
		error			   = inputError;
		type       		   = inputType;
		timeStamp  		   = inputTimeStamp;
		status 			   = inputStatus;
		destinationAddress = inputDestinationAddress;
	}
	
	public boolean getIsError()
	{
		return error;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getDestinationAddress()
	{
		return new String(destinationAddress);
	}
	
	public String getTimeStamp()
	{
		return new String(timeStamp);
	}
	
	public String getStatus()
	{
		return new String(status);
	}
	
	public static String findType(int inputType)
	{
		String textVal = new String();
		
		switch(inputType) 
		{
			//case -3: textVal = "System Error"; break;
			//case -2: textVal = "Logic Error"; break;
			
			case 0: textVal = "Unknown"; break;
			case 1: textVal = "Idel";    break;
			case 2: textVal = "Call";    break;
			case 3: textVal = "Text";    break;
			case 4: textVal = "Mail";    break;
        	case 5: textVal = "Web";     break;
        	case 6: textVal = "App";     break;
        	case 7: textVal = "GPS";     break;
        	case 8: textVal = "Server";  break;
		}
		
		return textVal;
	}
	
	public String toString()
	{
		String textVal = findType(type);
		/*
		switch(type) 
		{
			//case -3: textVal = "System Error"; break;
			//case -2: textVal = "Logic Error"; break;
			
			case 0: textVal = "Unknown"; break;
			case 1: textVal = "Idel";    break;
			case 2: textVal = "Call";    break;
			case 3: textVal = "Text";    break;
			case 4: textVal = "Mail";    break;
        	case 5: textVal = "Web";     break;
        	case 6: textVal = "App";     break;
        	case 7: textVal = "GPS";     break;
        	case 8: textVal = "Server";  break;
		}*/
		
		
		
		if(error)
		{
			return "!> "+timeStamp + " - " + textVal;
		}
		else
		{
			return timeStamp + " - " + textVal;
		}
	}
}
/*
 * LINKS
 * http://na.blackberry.com/eng/devjournals/resources/journals/jan_2005/api_spotlight_phone.jsp#phonecall_getCallId
 * http://today.java.net/pub/a/today/2009/07/02/blackberry-j2me-development.html
 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800901/How_to_-_Detect_system_availability_on_startup.html?nodeid=1396048&vernum=0
 * http://docs.blackberry.com/en/developers/deliverables/8540/Phone_565538_11.jsp
 * http://www.johnwargo.com/index.php/domino/45-dbja1
 */