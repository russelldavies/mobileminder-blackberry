package com.kids;

/**
 * Class handles getting the GPS coordinates, and the frequency it updates
 * http://docs.blackberry.com/ja-jp/developers/deliverables/29185/GPS_overview_1679738_11.jsp
 */

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;


/**
 * 
 * MyGPSListener monitors and registers location based events.
 *
 */

public class MyGPSListener extends Thread
{
	private LocalDataWriter actLog;
	private int 			timer;
			Debug     		logWriter		= Logger.getInstance();
			GPSMessage 		gpsMessage;

/**
 * The GPSListener constructor initialise the action store location and the interval value.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputGPSTimer interval value
 */

public MyGPSListener(LocalDataWriter inputAccess, int inputGPSTimer)
	{
		actLog = inputAccess;
		timer  = inputGPSTimer;
		gpsMessage = new GPSMessage();

		this.start();
	}
	
/**
 * When this function is started it poles the GPS service based on the interval specified in the constructor.
 * When the location changes this recorded in local data access.  
* 		Stores GPS location information about the device in the form of an action to the action log
* <p>   Its sets the criteria for the type of location information needed.
* 		It retrieves the location and saves it to the action log.
*
*/	
	
	
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
		        
				Location mylocation = LocationProvider.getInstance(criteria).getLocation(3);//Throws: NullPointerException
				//If there has been a change in the location of the device it is stored to the action log as an action of type GPS.
				if(mylocation.getSpeed()>0)
				{
					float  heading   = mylocation.getCourse();
		            double longitude = mylocation.getQualifiedCoordinates().getLongitude();
		            double latitude  = mylocation.getQualifiedCoordinates().getLatitude();
		            float  altitude  = mylocation.getQualifiedCoordinates().getAltitude();
		            float  speed     = mylocation.getSpeed();
					
		            /*
					actLog.addAction(action.TYPE_GPS,
									 "Course:"+heading+" Speed:"+speed,
									 "lon:"+longitude+" lat:"+latitude);
					*/
		            actLog.addMessage(gpsMessage);
				}
			}
		} 
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

	}	// end run()
}

