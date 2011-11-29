package com.mmtechco.mobileminder.monitor;

/**
 * Class handles getting the GPS coordinates, and the frequency it updates
 * http://docs.blackberry.com/ja-jp/developers/deliverables/29185/GPS_overview_1679738_11.jsp
 */

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Monitors and registers location based events.
 */
public class LocationMonitor extends Thread implements LocationListener {
	private static final String TAG = ToolsBB.getSimpleClassName(LocationMonitor.class);
	
	private LocalDataWriter actLog;
	private int 			timer;
	private	Logger     		logger	= Logger.getInstance();
	public  LocationMessage 		locationMessage;
	public  Location 		myLocation = null;
	public	float			speed;
	public	double 			longitude;
	public  double			latitude;


/**
 * The GPSListener constructor initialise the action store location and the interval value.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputGPSTimer interval value
 */

public LocationMonitor(LocalDataWriter inputAccess, int inputGPSTimer)
{
	logger.log(TAG, "MyGPSListener::Constructor");
	actLog 		= inputAccess;
	timer 		= inputGPSTimer;
	locationMessage	= new LocationMessage();
	
	// Initialise coords to 0
	speed = 0;
	latitude = 0;
	longitude = 0;
	
	this.start();
}
	
/**
 * When this function is started it poles the GPS service based on the interval specified in the constructor.
 * When the location changes this is recorded in local data access.  
* 		Stores GPS location information about the device in the form of an action to the action log
* <p>   Its sets the criteria for the type of location information needed.
* 		It retrieves the location and saves it to the action log.
*
*/	

	public void locationUpdated(LocationProvider arg0, Location arg1)
	{
		logger.log(TAG, "GPS Location changed");
		
		Criteria criteria = new Criteria();
		criteria.setHorizontalAccuracy(       Criteria.NO_REQUIREMENT);
        criteria.setVerticalAccuracy(         Criteria.NO_REQUIREMENT);
        criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
        criteria.setCostAllowed(true);
        
		try 
		{
			myLocation = LocationProvider.getInstance(criteria).getLocation(timer);
		} 
		//LocationException - if the location couldn't be retrieved or if the timeout period expired 
		//InterruptedException - if the operation is interrupted by calling reset() from another thread 
		//SecurityException - if the calling application does not have a permission to query the location information 
		//IllegalArgumentException - if the timeout = 0 or timeout < -1
		catch (LocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Check if coordinates have changed...
		if ( longitude != myLocation.getQualifiedCoordinates().getLongitude() ||
			  latitude != myLocation.getQualifiedCoordinates().getLatitude()   )
		{
			speed     = myLocation.getSpeed();
			longitude = myLocation.getQualifiedCoordinates().getLongitude();
	        latitude  = myLocation.getQualifiedCoordinates().getLatitude();
		}
        locationMessage.clearData();
        locationMessage.setMessage(latitude, longitude, speed);
        actLog.addMessage(locationMessage);		
	}
	
	public void providerStateChanged(LocationProvider provider, int newState)
	{
		logger.log(TAG, "GPS Provider changed");		
	}
	
	/*
	public void run()
	{
		try
	    {
			while(true)
			{
				sleep(timer);
				
				Criteria criteria = new Criteria();
				criteria.setHorizontalAccuracy(       Criteria.NO_REQUIREMENT);
		        criteria.setVerticalAccuracy(         Criteria.NO_REQUIREMENT);
		        criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
		        criteria.setCostAllowed(true);
		        
				Location mylocation = LocationProvider.getInstance(criteria).getLocation(3);
				//If there has been a change in the location of the device it is stored to the action log as an action of type GPS.
				if(mylocation.getSpeed()>0)
				{
		            float  speed     = mylocation.getSpeed();
					double longitude = mylocation.getQualifiedCoordinates().getLongitude();
		            double latitude  = mylocation.getQualifiedCoordinates().getLatitude();
		            //float  altitude  = mylocation.getQualifiedCoordinates().getAltitude();
					//float  heading   = mylocation.getCourse();
					
		            gpsMessage.setMessage(latitude, longitude, speed);
		            
		            actLog.addMessage(gpsMessage);
				} // end if
			} // end while
		} // end try
        
        //LocationException - if the location couldn't be retrieved or if the timeout period expired 
        //InterruptedException - if the operation is interrupted by calling reset() from another thread 
        //SecurityException - if the calling application does not have a permission to query the location information 
        //IllegalArgumentException - if the timeout = 0 or timeout < -1
        
        catch (LocationException e)
        {
        	logWriter.log("myGPSListener::run::LocationException::"+e.getMessage());
        	//actLog.addAction(true,action.TYPE_GPS,e.toString());
        }
        catch (InterruptedException e)
		{
        	logWriter.log("myGPSListener::run::InterruptedException::"+e.getMessage());
        	//actLog.addAction(true,action.TYPE_GPS,e.toString());
		}
        catch (Exception e)
		{
        	logWriter.log("myGPSListener::run::Exception::"+e.getMessage());
        	//actLog.addAction(true,action.TYPE_GPS,e.toString());
		}

	}	// end run()*/
}


/**
 * This class implements the message interface to hold GPS event messages.
 */
class LocationMessage implements Message {
	private final String type;
	private String deviceTime;
	public static double latitude = 0.0;
	public static double longitude = 0.0;
	public static float speed = 0;
	private MMTools tools;

	/**
	 * The constructor initialises all the message parameters
	 */
	public LocationMessage() {
		this(false);
	}

	/**
	 * The constructor initialises all the message parameters
	 */
	public LocationMessage(boolean sync) {
		if (sync) {
			type = "12";
		} else {
			type = "02";
		}
		clearData();
	}

	/**
	 * Adds details of the GPS event to the GPS object
	 * 
	 * @param _latitude
	 *            The latitude
	 * @param _longitude
	 *            The longitude
	 * @param _deviceTime
	 *            The Device time
	 */
	public void setMessage(double _latitude, double _longitude, float _speed) {
		clearData();
		latitude = _latitude;
		longitude = _longitude;
		speed = _speed;
		deviceTime = tools.getDate();
	}

	/**
	 * This method removes the current data in the message and initialises the
	 * parameters.
	 */
	public void clearData() {
		latitude = 0.0;
		longitude = 0.0;
		speed = 0;
		deviceTime = null;
	}

	/**
	 * This method retrieves the GPS coordinates in String form, seperated by a
	 * comma
	 * 
	 * @return the GPS coordinates seperated by a comma
	 */
	public String getLocation() {
		return latitude + Tools.ServerQueryStringSeparator + longitude;
	}

	/**
	 * This method retrieves the message formatted in to a single string value.
	 * <p>
	 * SMS message consists of:
	 * <ul>
	 * <li>Registration Serial number.
	 * <li>Type of SMS message.
	 * <li>Device time.
	 * <li>Phone number
	 * <li>Status ( Incoming or outgoing ).
	 * <li>Status ( Delivered or Bounced )
	 * <li>The body of the SMS message.
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + type
				+ Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + getLocation()
				+ Tools.ServerQueryStringSeparator + getSpeed();
	}

	/**
	 * This method retrieves the speed at which the handset is travelling, as a
	 * float
	 * 
	 * @return The speed
	 */
	private float getSpeed() {
		return speed;
	}

	/**
	 * This method retrieves the time that is set on the device.
	 * 
	 * @return the device time
	 */
	public String getTime() {
		return deviceTime;
	}

	/**
	 * This method retrieves the type number for the SMS message
	 * 
	 * @return the type number corresponding to a SMS message
	 */
	public int getType() {
		return Integer.parseInt(type);
	}

	public void setLocation(double _latitude, double _longitude) {
		latitude = _latitude;
		longitude = _longitude;
	}
}