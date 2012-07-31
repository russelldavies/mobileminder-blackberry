//#preprocess
package com.mmtechco.mobileminder.monitor;

import javax.microedition.location.LocationException;

import net.rim.device.api.gps.BlackBerryLocation;
//#ifdef BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.gps.LocationInfo;
//#endif
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import rimx.location.simplelocation.SimpleLocationListener;
import rimx.location.simplelocation.SimpleLocationProvider;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors and registers location based events.
 */
public class LocationMonitor {
	private static Logger logger = Logger.getLogger(LocationMonitor.class);

	private static SimpleLocationProvider simpleProvider;
	private static BlackBerryLocation location;
	
	// Represents the period of the position query, in seconds
	private static int interval = 120;
	
	public static double latitude;
	public static double longitude;

	public static void start() {
		if (simpleProvider != null) {
			return;
		}

		//#ifdef BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		LocationInfo.setLocationOn();
		//#endif
		try {
			simpleProvider = new SimpleLocationProvider();
		} catch (final IllegalStateException e) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Dialog.alert(e.getMessage());
					return;
				}
			});
		} catch (LocationException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		}
		if (simpleProvider != null) {
			simpleProvider.addSimpleLocationListener(new LocationListener(), interval);
		}

	}
	
	public static void stop() {
		if (simpleProvider != null) {
			simpleProvider.removeSimpleLocationListener();
		}
	}
	
	private static class LocationListener implements SimpleLocationListener {
		public void locationEvent(int event, Object eventData) {
			if(event == SimpleLocationListener.EVENT_GPS_LOCATION){
				location = (BlackBerryLocation)eventData;
				logger.info("GPS");
			} else if(event == SimpleLocationListener.EVENT_CELL_GEOLOCATION){
				location = (BlackBerryLocation)eventData;
				logger.info("Cell Tower Geolocation");
			} else if(event == SimpleLocationListener.EVENT_WLAN_GEOLOCATION){
				location = (BlackBerryLocation)eventData;
				logger.info("WLAN Geolocation");
			} else if(event == SimpleLocationListener.EVENT_UNKNOWN_MODE){
				location = (BlackBerryLocation)eventData;
				logger.info("Unknown");
			} else if(event == SimpleLocationListener.EVENT_ACQUIRING_LOCATION){
				logger.info("EVENT_ACQUIRING_LOCATION - attempt = " + eventData);	
			} else if(event == SimpleLocationListener.EVENT_LOCATION_FAILED){
				logger.info("EVENT_LOCATION_FAILED - attempt = " + eventData);	
			}
			
			if (location != null && location.isValid()) {
				latitude = location.getQualifiedCoordinates().getLatitude();
				longitude = location.getQualifiedCoordinates().getLongitude();
				float speed = location.getSpeed();
				logger.debug("Location: " + latitude + "," + longitude);
				
				ActivityLog.addMessage(new LocationMessage(latitude, longitude, speed));
			}
		}

		public void debugLog(String msg) {
			logger.debug(msg);
		}
		
	}
}

class LocationMessage extends Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>Device time
	 * <li>Latitude
	 * <li>Longitude
	 * <li>Speed
	 * </ul>
	 */
	public LocationMessage(double lat, double lon, float speed) {
		super(Message.LOCATION,
				new String[] { ToolsBB.getInstance().getDate(),
						String.valueOf(lat), String.valueOf(lon),
						String.valueOf(speed) });
	}
}