package com.mmtechco.mobileminder.monitor;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.ErrorMessage;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Monitors and registers location based events.
 */
//public class LocationMonitor extends Thread implements LocationListener {
public class LocationMonitor implements LocationListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(LocationMonitor.class);

	private LocalDataWriter actLog;
	private Logger logger = Logger.getInstance();
	public LocationMessage locationMessage;
	private int interval;
	public Location myLocation = null;

	private float speed;
	private double longitude;
	private double latitude;

	public LocationMonitor(LocalDataWriter actLog, int interval) {
		this.actLog = actLog;
		this.interval = interval;
		locationMessage = new LocationMessage();

		// Initialize coordinates
		speed = 0;
		latitude = 0;
		longitude = 0;
	}

	public void locationUpdated(LocationProvider provider, Location location) {
		// Polls GPS service based on interval specified in constructor. When
		// location changes record in activity log.
		logger.log(TAG, "GPS Location changed");

		Criteria criteria = new Criteria();
		criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
		criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
		criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
		criteria.setCostAllowed(true);

		try {
			myLocation = LocationProvider.getInstance(criteria).getLocation( interval);
		} catch (LocationException e) {
			logger.log(TAG, "Location could not be retrieved");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Check if coordinates have changed
		if (longitude != myLocation.getQualifiedCoordinates().getLongitude()
				|| latitude != myLocation.getQualifiedCoordinates()
						.getLatitude()) {
			speed = myLocation.getSpeed();
			longitude = myLocation.getQualifiedCoordinates().getLongitude();
			latitude = myLocation.getQualifiedCoordinates().getLatitude();
		}
		locationMessage.clearData();
		locationMessage.setMessage(latitude, longitude, speed);
		actLog.addMessage(locationMessage);
	}

	public void providerStateChanged(LocationProvider provider, int newState) {
		logger.log(TAG, "GPS Provider changed");
	}

	/*
	public void run() {
		while (true) {
			try {
				sleep(interval);

				Criteria criteria = new Criteria();
				criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
				criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
				criteria.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);
				criteria.setCostAllowed(true);

				Location location = LocationProvider.getInstance(criteria)
						.getLocation(3);
				// If there has been a change in the location of the device it
				// is
				// stored to the action log as an action of type GPS.
				if (location.getSpeed() > 0) {
					float speed = location.getSpeed();
					double longitude = location.getQualifiedCoordinates()
							.getLongitude();
					double latitude = location.getQualifiedCoordinates()
							.getLatitude();
					// float altitude =
					// mylocation.getQualifiedCoordinates().getAltitude();
					// float heading = mylocation.getCourse();

					locationMessage.setMessage(latitude, longitude, speed);
					actLog.addMessage(locationMessage);
				}
			} catch (LocationException e) {
				actLog.addMessage(new ErrorMessage(e));
				logger.log(TAG, e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	*/
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
		return Registration.getRegID() + Tools.ServerQueryStringSeparator
				+ type + Tools.ServerQueryStringSeparator + deviceTime
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