package com.spotlight.track;

import java.util.Date;
import java.util.Vector;
import java.io.IOException;
import java.io.InterruptedIOException;
  
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

/**
 * 
 * LocalDataAccess is used by the Listener objects to store triggered events that occur in the system.
 *
 */
public class LocalDataAccess
{	
	private PersistentObject store;//HD
	private Vector LocalData;      //list of Actions
	
/**
 * 
 * The constructor calls the setupStore method
 *
 */
	
	public LocalDataAccess()
	{
		setupStore();
	}
	
/**
 * 
 * This function returns the current date and time in a formatted string of 12 digits ending with the seconds.
 * <p><b>Example:</b>
 * An example of this is 31st of August 2010 would be represented as 100831105221
 * 
 * @return
 * String containing formatted date
 * 
 */
	
	public String getDate()
	{
		//return new SimpleDateFormat("HH:mm:ss dd-MM-yy").format(new Date());
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}
	
/**
 * 
 * This function creates a persistent storage object which hold a Vector to store triggered actions on the device.
 * 
 */
	
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
//TODO addAction function should return a boolean to ensure action has been stored
/**
 * 
 * This function adds an action to the persistent storage on the device.
 * @param  inputType type of action
 * @param  inputStatus status of the action
 */
	public void addAction(int inputType, String inputStatus)
	{
		addAction(false, inputType,inputStatus);
	}

/**
 * 
 * This function adds an action to the persistent storage on the device.
 * @param  inputError error status
 * @param  inputType type of action
 * @param  inputStatus status of the action
 */
	public void addAction(boolean inputError, int inputType, String inputStatus)
	{
		addAction(inputError, inputType,inputStatus,"");
	}

/**
 * This function adds an action to the persistent storage on the device.
 * @param  inputType type of action
 * @param  inputStatus status of the action
 * @param  inputDestinationAddress action address
 */
	public void addAction(int  inputType, 
						  String inputStatus,
						  String inputDestinationAddress)
	{
		addAction(false,inputType,inputStatus,inputDestinationAddress);
	}
	
/**
 * This function adds an action to the persistent storage on the device.
 * @param error error status
 * @param  inputType type of action
 * @param  inputStatus status of the action
 * @param  inputDestinationAddress action address
 */
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
	
/**
 * Retrieves an action from the persistent storage based on an index
 * @param index action identifier
 * @return Action object.
 */
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
	
/**
* Removes an action from the persistent storage based on an index
* @param index action identifier
* @return boolean true if the action has been removed
*/
		
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

/**
 * Retrieves the size of the object
 * @return integer size of the object
 */
	public int length()
	{
		synchronized (store)
		{
			LocalData = (Vector)store.getContents();
			return LocalData.size();
		}
	}
}

/**
 * 
 * The action class is used to instantiate action objects to remain persistent in the devices storage.
 *
 */
public class action implements Persistable
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

/**
 * Creates a default action object when no parameters are provided
 */
	public action()
	{
		this(false,action.TYPE_UNKNOWN,"","","");
	}

/**
 * Creates an action object with with a integer error input and stores it as a boolean
 * 
 * @param  inputError error status
 * @param  inputType action type
 * @param  inputTimeStamp time of action occurrence
 * @param  inputStatus action details
 * @param  inputDestinationAddress action address
 */
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

/**
* Creates an action object with with a boolean error input
* 
* @param  inputError error status
* @param  inputType action type
* @param  inputTimeStamp time of action occurrence
* @param  inputStatus action details
* @param  inputDestinationAddress action address
*/
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
	
/**
 * retrieves error status of an action
 * 
 * @return error value
 */
	public boolean getIsError()
	{
		return error;
	}

/**
 * retrieves the type of an action
 * 
 * @return type value
 */
	public int getType()
	{
		return type;
	}
	
/**
 * retrieves the action address
 * 
 * @return action address
 */
	public String getDestinationAddress()
	{
		return new String(destinationAddress);
	}

/**
 * retrieves the time of the action
 * 
 * @return time of action
 */
	public String getTimeStamp()
	{
		return new String(timeStamp);
	}

/**
 * retrieves the details about the action
 * 
 * @return action details
 */
	public String getStatus()
	{
		return new String(status);
	}

/**
 * retrieves the name of an action type based on a type number
 * 
 * @param  inputType number representing a type
 * @return name of the corresponding type
 */
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

/**
 * This function overrides the toString method inherent to all object.
 * It facilitates the pursuit of more detailed information about the action object.
 * 
 *@return String containing the error status, time of action and action type.
 */
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
