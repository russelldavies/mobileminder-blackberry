package com.kids.Monitor;

/**
 * Class handles getting the GPS coordinates, and the frequency it updates
 * http://docs.blackberry.com/ja-jp/developers/deliverables/29185/GPS_overview_1679738_11.jsp
 */

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import com.kids.Logger;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;


/**
 * 
 * MyGPSListener monitors and registers location based events.
 *
 */

public class MyGPSListener extends Thread implements LocationListener
{
	private LocalDataWriter actLog;
	private int 			timer;
	private	Debug     		logWriter	= Logger.getInstance();
	public  GPSMessage 		gpsMessage;
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

public MyGPSListener(LocalDataWriter inputAccess, int inputGPSTimer)
{
	logWriter.log("MyGPSListener::Constructor");
	actLog 		= inputAccess;
	timer 		= inputGPSTimer;
	gpsMessage	= new GPSMessage();
	
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
		logWriter.log("GPS Location changed");
		
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
        gpsMessage.clearData();
        gpsMessage.setMessage(latitude, longitude, speed);
        actLog.addMessage(gpsMessage);		
	}
	
	public void providerStateChanged(LocationProvider provider, int newState)
	{
		logWriter.log("GPS Provider changed");		
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