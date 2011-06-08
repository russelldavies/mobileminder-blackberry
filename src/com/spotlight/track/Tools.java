package com.spotlight.track;

import java.util.Date;
import m.java.util.regex;

import com.kids.net.ErrorMessage;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

public class Tools 
{
	private static String returnVer="0";
	private static com.kids.prototypes.LocalDataReader actLog = LocalDataAccess.getLocalDataAccessRef();
	private static m.java.util.regex.Pattern pattern = Pattern.compile( "([0-9]*)\\.[0]" );
	private static Matcher matcher;
	
	 public static int getGenOSVersion()
	 {
		 // getOSVersion returns a string in form x.yy.zz
		 // We just need the "x", so get that as a substring
        return Integer.parseInt((Tools.getOSVersion()).substring(0, 1));
	 }
	 
	 /**
	  * Get the OS version of the current handset
	  * @return OS Version is String format
	  */
    public static String getOSVersion()
    {
    	if(returnVer.equals("0"))
    	{
	        //USING THE APPLICATION MANAGER
	        //(RUNNING APPS)
	    	
	        //get the ApplicationManager
	        ApplicationManager appMan = ApplicationManager.getApplicationManager();
	
	        //grab the running applications
	        ApplicationDescriptor[] appDes = appMan.getVisibleApplications();
	        //check for the version of a standard
	
	        //RIM app. I like to use the ribbon app but you can check the version of 
	        //any RIM module as they will all be the same.
	       int size = appDes.length;
	       for (int count = size-1; count>=0; --count)
	       {
	           if ((appDes[count].getModuleName()).equals("net_rim_bb_ribbon_app"))
	           {
	        	   returnVer = appDes[count].getVersion();
	           } // End if()
	       }	// End for()
    	}
       return returnVer;
    }	// End getOSVersion
    
	/**
	 * Gets the correct format of date in specified format.
	 * @param _date milliseconds since the epoch.
	 * @return Date in milliseconds value in specified format.
	 * @throws throws ParseException when input date format is not correct.
	 */	
	public static long getDate(String _date)// throws ParseException
	{
		if("0".equals(_date))
		{	return 0;	}
		else
		{	return new Date().getTime(); }
	}
	
	/**
	 * Gets the string format of date in specified format.
	 * @param _date milliseconds since the epoch.
	 * @return
	 */
	public static String getDate(long _date)
	{
		return new SimpleDateFormat("yyMMddHHmmssZ").format(new Date(_date)).substring(0, 15);//the GTM return time zone in xxxx hours we only need xx
	}
	
	/**
	 * Gets the String format of date.
	 * @return string which contains the date.
	 */
	public static String getDate()
	{
		//return new SimpleDateFormat("HH:mm:ss dd-MM-yy").format(new Date());
		//return new SimpleDateFormat("yyMMddHHmmssZ").format(new Date()).substring(0, 15);//the GTM return time zone in xxxx hours we only need xx
		return getDate(new Date().getTime());
	}

	/**
	 * Converts a number which in string format to integer format.
	 * @param _text a number which in string format.
	 * @return a number which in integer format.
	 */
	public static int txt2num(String _text) {
		try
		{
			return Integer.parseInt(_text.trim());
		}
		catch(Exception e)
		{
			actLog.addMessage(new ErrorMessage(e));
			//actLog.addMessage();
			return -1;	
		}
	}

	/**
	 * Checks the inputNumber to check whether it matches the number pattern.
	 * @param _text a number.
	 * @return boolean true if number matches with the pattern false otherwise.
	 */
	public static synchronized boolean isNumber(String _text)
	{
		matcher = pattern.matcher(_text);
		return matcher.matches();
	}

}
