package com.mmtechco.mobileminder;

import net.rim.device.api.ui.UiApplication;
 

/**
 * Main entry point of the application.
 */
class Driver extends UiApplication
{
    /**
     * Entry point for application
     * @param args Command line arguments (not used)
     */ 
        public static void main(String[] args)
    {
            // Create a new instance of the application and make the currently
            // running thread the application's event dispatch thread.
        	new Driver().enterEventDispatcher();
    }
     
/**
 * Initialises the objects that will register themselves with the appropriate event listeners
 */
    public Driver()
    {
        int employerID  = 1;
        int deviceID    = 2;
        int oneSec        = 1000;
        int uploadTimer =  1*oneSec;//send update every
        int GPSTimer    = 15*oneSec;//check GPS every
        int AppTimer    =  2*oneSec;//check running app every
        
        LocalDataAccess actLog = new LocalDataAccess();
        actLog.addAction(action.TYPE_IDEL,"Starting Controler");
    
        //actLog.removeAction(0);
        new MyCallListener(actLog);
        new MyTextListener(actLog);
        new MyMailListener(actLog);
        new MyGPSListener (actLog, GPSTimer);
        new MyAppListener (actLog, AppTimer);
        new MyServerUpload(actLog, employerID, deviceID, uploadTimer);
    }
    
}