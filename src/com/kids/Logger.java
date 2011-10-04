package com.kids;

import com.kids.prototypes.Debug;

public class Logger implements Debug {

	private static Logger logger;
	
	public Logger(){}
	
	public static Debug getInstance(){
		if(logger == null)
		{logger = new Logger();}
		
		return logger;
		}
	
    /**
     * This is a central point that can be called from anywhere in the code base to display information to the user.
     * 
     * @param _message information text that is passed.
     */
	    public void log(String _message)
	    {
	    	System.err.println("CVK::"+_message);
	    	//Log.v("ClearViewKids", "CVK :: "+_message);
	    }
}
