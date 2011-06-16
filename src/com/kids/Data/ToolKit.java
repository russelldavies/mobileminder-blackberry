package com.kids.Data;

import java.util.Date;
import java.util.Random;


import com.kids.prototypes.MMTools;

public abstract class ToolKit implements MMTools
{
	//private static Date deviceStartTime;
	public static final String RestElementSeparator = ",";

	public static int stopWatchTime;

	/**
	 * This method get the time in second from when the device booted.
	 * @return an integer representing the up-time in seconds.
	 */	
	abstract public int getUptimeInSec();
/**
 * This method converts a safely converts a long integer into a standard integer	
 * @param l long integer
 * @return standard version
 */
	public int safeLongToInt(long l) 
	{
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) 
	    {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}
	

	/**
	 * Gets the correct format of date in specified format.
	 * @param _date milliseconds since the epoch.
	 * @return Date in milliseconds value in specified format.
	 * @throws throws ParseException when input date format is not correct.
	 */	
	abstract public long getDate(String _date);
	
	/**
	 * Gets the string format of date in specified format.
	 * @param _date milliseconds since the epoch.
	 * @return
	 */
	abstract public String getDate(long _date);

	
	/**
	 * Gets the String format of date.
	 * @return string which contains the date.
	 */
	public String getDate()
	{
		//return new SimpleDateFormat("HH:mm:ss dd-MM-yy").format(new Date());
		//return new SimpleDateFormat("yyMMddHHmmssZ").format(new Date()).substring(0, 15);//the GTM return time zone in xxxx hours we only need xx
		return getDate(new Date().getTime());
	}
	

   public String calcHM(long timeInSeconds) {
      //long hours, minutes, seconds;
      timeInSeconds = timeInSeconds / 1000;
      
     /* hours = timeInSeconds / 3600;
      //timeInSeconds = timeInSeconds - (hours * 3600);
      minutes = (timeInSeconds % 3600) / 60;
      //timeInSeconds = timeInSeconds - (minutes * 60);
      seconds = (minutes % 60) / 60;
      
      String time = hours + ":" + minutes + ":"+seconds;
      
      return time;
      */
      return Long.toString(timeInSeconds);
   }
         
	
	/**
	 * Converts a number which in string format to integer format.
	 * @param _text a number which in string format.
	 * @return a number which in integer format.
	 */
	public int txt2num(String _text)
	{
		try
		{
			return Integer.parseInt(_text.trim());
		}
		catch(Exception e)
		{	//LocalDataAccess.addOtherMessage(new ErrorMessage(e));
			return -1;	
		}
	}
	/**
	 * Checks the inputNumber to check whether it matches the number pattern.
	 * @param _text a number.
	 * @return boolean true if number matches with the pattern false otherwise.
	 */
	abstract public boolean isNumber(String _text);
	
	/**
	 * This method formats a string into a hex string
	 * 
	 * @param b string
	 * @return hex string
	 */
	 public String stringToHex(String s)
	 {
		char[] b;
		b = s.toCharArray();
	//s	s.getBytes();
		 
	    StringBuffer sb = new StringBuffer(b.length * 2);
	    for (int i = 0; i < b.length; i++) 
	    {	int v = b[i] & 0xff;
	    	if (v < 16){	sb.append('0');}
	    	sb.append(Integer.toHexString(v));
	    }
	    	return sb.toString().toUpperCase();
	 }
	
	/**
	 * This method formats a HEX string, adding a random HEX value to the start and end of the string
	 * 
	 * @param _input HEX string
	 * @return hex string
	 */
	public String topAndTail(String _input)
	{
		Random rand = new Random();
		int top = rand.nextInt(16);
		int tail = rand.nextInt(16);
		String hexTop = Integer.toHexString(top);
		String hexTail = Integer.toHexString(tail);
		//Top=======Hex_string=======tail
		hexTop = hexTop.concat(_input).concat(hexTail);
		//logger.log("topAndTail:returns:"+hexTop);
		return hexTop;
	}
	
	/**
	 * This method formats a HEX string, removing a random HEX value from the start and end of the string
	 * 
	 * @param _input HEX string
	 * @return hex string
	 */
	public String reverseTopAndTail(String _input)
	{
		String returnString = _input.substring(1, (_input.length() - 1));
		//logger.log("reversetopAndTail:returns:"+returnString);
		return returnString;
	}
}
