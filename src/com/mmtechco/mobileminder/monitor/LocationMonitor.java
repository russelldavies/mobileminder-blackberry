//#preprocess
package com.mmtechco.mobileminder.monitor;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
import net.rim.device.api.gps.BlackBerryCriteria;
import net.rim.device.api.gps.BlackBerryLocationProvider;
//#endif
//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1
import net.rim.device.api.gps.GPSInfo;
//#endif
//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0 | VER_5.0.0
import net.rim.device.api.gps.LocationInfo;
//#endif

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.ErrorMessage;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Monitors and registers location based events.
 */
public class LocationMonitor implements LocationListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(LocationMonitor.class);

	private Logger logger = Logger.getInstance();

	// Represents the period of the position query, in seconds
	private static int interval = 120;

	//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
	private BlackBerryLocationProvider locationProvider;
	//#else
	private LocationProvider locationProvider;
	//#endif
	private double latitude;
	private double longitude;

	public LocationMonitor() throws LocationException {
		//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0 | VER_5.0.0
		// Enable location services
		if (LocationInfo.getAvailableLocationSources() != 0) {
			LocationInfo.setLocationOn();
		//#else
        if (LocationProvider.getInstance(null) == null) {
		//#endif
			// Attempt to start the listening thread
			if (startLocationUpdate()) {
				logger.log(TAG,
						"Location status: " + locationProvider.getState());
			}
		} else {
			logger.log(TAG, "Could not start location services");
			return;
		}

		// Initialize lat/long
		latitude = 0;
		longitude = 0;
	}

	public boolean startLocationUpdate() {
		boolean started = false;

		try {
			//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
			BlackBerryCriteria criteria = new BlackBerryCriteria(GPSInfo.GPS_MODE_ASSIST);
			criteria.enableGeolocationWithGPS();
			criteria.setFailoverMode(GPSInfo.GPS_MODE_AUTONOMOUS, 3, 100);
			//criteria.setSubsequentMode(GPSInfo.GPS_MODE_CELLSITE);
			//#else
			Criteria criteria = new Criteria();
			// criteria.setMode(GPSInfo.GPS_MODE_AUTONOMOUS);
			criteria.setCostAllowed(true);
			//#endif
			criteria.setHorizontalAccuracy(5);
			criteria.setVerticalAccuracy(5);
			criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_MEDIUM);
			criteria.setPreferredResponseTime(10000);

			//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
			locationProvider = (BlackBerryLocationProvider) LocationProvider
					.getInstance(criteria);
			//#else
			locationProvider = LocationProvider.getInstance(criteria);
			//#endif

			if (locationProvider != null) {
				/*
				 * Only a single listener can be associated with a provider, and
				 * unsetting it involves the same call but with null. Therefore,
				 * there is no need to cache the listener instance request an
				 * update every second.
				 */
				locationProvider.setLocationListener(this, interval, -1, -1);
				started = true;
			} else {
				logger.log(TAG, "Failed to obtain a location provider.");
			}
		} catch (final LocationException le) {
			logger.log(TAG, "Failed to instantiate LocationProvider object:"
					+ le.toString());
			ActivityLog.addMessage(new ErrorMessage(le));
		}
		return started;
	}

	public void locationUpdated(LocationProvider provider, Location location) {
		// Polls GPS service based on interval specified in constructor. When
		// location changes record in activity log.
		if (location.isValid()) {
			float speed;
			// Check if coordinates have changed
			if (longitude != location.getQualifiedCoordinates().getLongitude()
					|| latitude != location.getQualifiedCoordinates()
							.getLatitude()) {
				speed = location.getSpeed();
				longitude = location.getQualifiedCoordinates().getLongitude();
				latitude = location.getQualifiedCoordinates().getLatitude();

				Message locMsg = new LocationMessage(longitude, latitude, speed);
				ActivityLog.addMessage(locMsg);
			}
		}
	}

	public void providerStateChanged(LocationProvider provider, int newState) {
		logger.log(TAG, "GPS Provider changed");
		if (newState == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			provider.reset();
		}
	}

	public void stopLocation() {
		if (locationProvider != null) {
			locationProvider.reset();
			locationProvider.setLocationListener(null, -1, -1, -1);
		}
	}
}

/**
 * Holds GPS messages
 */
class LocationMessage implements Message {
	private final int type = 6;
	private double latitude, longitude;
	private String deviceTime;
	private float speed;

	public LocationMessage(double lat, double lon, float speed) {
		latitude = lat;
		longitude = lon;
		this.speed = speed;
		deviceTime = ToolsBB.getInstance().getDate();
	}

	/**
	 * Retrieves the message formatted in to a single string value. Location
	 * message consists of:
	 * <ul>
	 * <li>Registration Serial number.
	 * <li>Location message type which is '06' (two digits number).
	 * <li>Device time.
	 * <li>Latitude.
	 * <li>Longitude.
	 * <li>Speed. *<i>Warning</i> not implemented
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + '0'
				+ type + Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + latitude
				+ Tools.ServerQueryStringSeparator + longitude
				+ Tools.ServerQueryStringSeparator + speed;
	}

	public String getTime() {
		return deviceTime;
	}

	public int getType() {
		return type;
	}
}