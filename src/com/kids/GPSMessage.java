package com.kids;

import com.kids.prototypes.Message;

/**
 * This class implements the message interface to hold GPS event messages.
 */
public class GPSMessage implements Message
{
	private final 	String 			type;
	private 		String 			deviceTime;
	private 		StringBuffer 	stringREST;
	public  static  double			latitude  = 0.0;
	public  static  double			longitude = 0.0;
	public  static  float			speed 	  = 0;
	//private MMTools tools = Tools.getInstance();
	
/**
 * The constructor initialises all the message parameters
 */
	public GPSMessage()
	{	this(false);	}
	
/**
 * The constructor initialises all the message parameters
 */
	public GPSMessage(boolean sync)
	{	
		if(sync)
		{	type = "12";	}
		else
		{	type = "02";	}
		clearData();	
	}


	/**
	 * Adds details of the GPS event to the GPS object
	 * @param _latitude The latitude
	 * @param _longitude The longitude
	 * @param _deviceTime The Device time
	 */
	public void setMessage(double _latitude, double _longitude, float _speed, String _deviceTime)
	{
		clearData();
		latitude   = _latitude;
		longitude  = _longitude;
		speed	   = _speed;
		deviceTime = _deviceTime;
	}
	
/**
 * This method removes the current data in the message and initialises the parameters. 
 */
	public void clearData()
	{
		stringREST = null;
		latitude   = 0.0;
		longitude  = 0.0;
		speed	   = 0;
		deviceTime = null;
	}

/**
 * This method retrieves the GPS coordinates in String form, seperated by a comma
 * @return the GPS coordinates seperated by a comma
 */
	public String getLocation() 
	{	return latitude+Tools.RestElementSeparator+longitude;	}
	
/**
 * This method retrieves the message formatted in to a single string value.
 * <p>
 * SMS message consists of:
 * <ul>
 * <li> Registration Serial number.
 * <li> Type of SMS message.
 * <li> Device time.
 * <li> Phone number
 * <li> Status ( Incoming or outgoing ).
 * <li> Status ( Delivered or Bounced )
 * <li> The body of the SMS message.
 * </ul>
 * @return a single string containing the entire message.
 */
	public String getREST() 
	{
		if(null == stringREST)
		{
			stringREST = new StringBuffer();
			stringREST.append(Registration.getRegID());
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(type);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(deviceTime);
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(getLocation());	// getLocation() returns "lat,lon", already comma seperated
			stringREST.append(Tools.RestElementSeparator);
			stringREST.append(getSpeed());			
		}
		return stringREST.toString();
	}
	
	/**
	 * This method retrieves the speed at which the handset is travelling, as a float
	 * @return The speed
	 */
	private float getSpeed()
	{	return speed;}

/**
 * This method retrieves the time that is set on the device.
 * 
 * @return the device time
 */
	public String getTime() 
	{	return deviceTime;	}
	
/**
 * This method retrieves the type number for the SMS message
 * 
 * @return the type number corresponding to a SMS message
 */
	public int getType() 
	{	return Integer.parseInt(type);	}
	
	public void setLocation(double _latitude, double _longitude)
	{
		latitude = _latitude;
		longitude = _longitude;
	}
	
}