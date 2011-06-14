package com.spotlight.track;

import java.util.Date;
import java.util.Vector;

import com.kids.prototypes.LocalDataReader;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.ui.text.NumericTextFilter;
import net.rim.device.api.util.AbstractString;
import net.rim.device.api.util.AbstractStringWrapper;

public class Tools 
{
	private static String returnVer="0";
	//private static com.kids.prototypes.LocalDataReader actLog = LocalDataAccess.getLocalDataAccessRef();
	private static LocalDataReader actLog = LocalDataAccess.getLocalDataAccessRef();
	
	//http://www.blackberry.com/developers/docs/4.7.0api/net/rim/device/api/ui/text/NumericTextFilter.html
	//private static m.java.util.regex.Pattern pattern = Pattern.compile( "([0-9]*)\\.[0]" );
	//private static Matcher matcher;
	
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
	public static String getDate(long _date)//the GTM return time zone in xxxx hours we only need xx
	{return new SimpleDateFormat("yyMMddHHmmssZ").format(new Date(_date)).substring(0, 12);}
	
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
			//actLog.addMessage(new ErrorMessage(e));
			//actLog.addMessage();
			return -1;	
		}
	}

	/**
	 * Checks the inputNumber to check whether it matches the number pattern.
	 * @param _text a number.
	 * @return boolean true if number matches with the pattern false otherwise.
	 */
	/*
	public static synchronized boolean isNumber(String _text)
	{
		//AbstractString aS;
		//aS=AbstractString.valueOf(_text);
		NumericTextFilter sdf;
		sdf.
		//Integer.parseInt(_text);
		return false;//.validate(_text);
		
		
		//http://www.blackberry.com/developers/docs/4.7.0api/net/rim/device/api/ui/text/NumericTextFilter.html
		//matcher = pattern.matcher(_text);
		//return matcher.matches();
	}*/
	 
	/**
	 * Method to split string by given seperator
	 * @param original - Original string to be split
	 * @param separator - character you want string to be split by
	 * @return result - String [] with String split into elements
	 */
	public static String[] split(String original,String separator) {
	    Vector nodes = new Vector();
	    // Parse nodes into vector
	    int index = original.indexOf(separator);
	    while(index >= 0) 
		{
	        nodes.addElement( original.substring(0, index) );
	        original = original.substring(index+separator.length());
	        index = original.indexOf(separator);
	    }
	    // Get the last node
	    nodes.addElement( original );

	     // Create split string array
	    String[] result = new String[ nodes.size() ];
	    if( nodes.size() > 0 )
		{
	        for(int loop = 0; loop < nodes.size(); loop++)
	        {
	            result[loop] = (String)nodes.elementAt(loop);
	            System.out.println(result[loop]);
	        }
	    }
	   return result;
	}	
}