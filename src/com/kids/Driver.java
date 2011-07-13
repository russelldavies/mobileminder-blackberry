/**
 * 
 * Driver contains the main entry point of the application, 
 * this is housed within the controller class. 
 * 
 */

package com.kids;

import javax.microedition.io.file.FileSystemListener;

import com.kids.Data.Tools;
import com.kids.Monitor.MyCallListener;
import com.kids.Monitor.MyGPSListener;
import com.kids.Monitor.MyMailListener;
import com.kids.Monitor.MyTextListener;
import com.kids.net.Server;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;

import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.UiApplication;
 
/**
 * 
 * Driver is called by the Application Manager after it calls the Application.
 *  
 * The controller registers objects with the system to monitor device actions
 *
 */

public class Driver extends UiApplication implements SystemListener2, FileSystemListener
{
	// Enable logging
    static  Debug 			logWriter     = Logger.getInstance();
	public  boolean			sdCardMounted = false;
	private Registration 	Reg;
			LocalDataReader actLog ;
	public 	boolean			isAutoStart	  = false;
            
    /**
     * After calling to enterEventDispatcher() the application enters the event-processing loop.
     * 
     *
     */
    public static void main(String[] args)
    {
        //How to create proper startup apps:
        //http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800332/832062/How_To_-_Write_safe_initialization_code.html?nodeid=1487426&vernum=0
    	Driver theApp;
      //Check for the argument defined in the project properties.
        if (args != null && args.length > 0 && args[0].equals("icon"))
        { // Started from icon click
        	logWriter.log("Icon clicked");
        	theApp = new Driver(false);
        } 
        else 
        {   //Started at boot
        	theApp = new Driver(true);
            // If system startup is still in progress when this
            // application is run.
            if (ApplicationManager.getApplicationManager().inStartup())
            {
                logWriter.log("Still starting up");
                theApp.addSystemListener(theApp);
            }
            else
            {
                logWriter.log("Fully booted up");
                theApp.doStartupWorkLater();
            }

        }      
        theApp.enterEventDispatcher();
        
    }
       
/**
 * Initialises the objects that will register themselves with the appropriate event listeners
 */
    //public Controller()
    Driver(boolean _isAutoStart)
    {	
    	// Now we know if the app is started at boot or by clicking the icon
    	if (!_isAutoStart)
    	{
    		requestForeground();
    		pushScreen( new serialScreen() ); 
    		
    	}
    }
    
    /**
     * Listens for a change in the root list, eg if a new volume is mounted
     * We're only interested in the SD card here
     */
	public void rootChanged(int state, String rootName)
	{ // I think this whole method is useless, except if someones removes/inserts a new SD card!
		if( state == ROOT_ADDED ) 
		{ 
			if( rootName.equalsIgnoreCase("sdcard/") )
			{ 
				//microSD card inserted 
				logWriter.log("Driver::SD Card inserted");
				sdCardMounted = true;
			}	 
		}
		else if( state == ROOT_REMOVED )
		{ 
			//perform the same check as above 
			logWriter.log("Driver::SD Card removed");
			sdCardMounted = false;
		} 
	}
	
    private void doStartupWorkLater()
    {
		actLog = LocalDataAccess.getLocalDataAccessRef();

    	invokeLater(new Runnable()
    	{
    		public void run()
    		{
    			
    			
    			///////////////    			
    			// Wait for SD Card to be mounted
				try {
	    			//while (!sdCardMounted)	// sdCardMounted will be changed to true by Listener above
					while(!Tools.hasSDCard())
					{
						logWriter.log("Driver::Waiting on SD card to be mounted...");
						Thread.sleep(1000*6);//6 seconds
					}
				} 
				catch (InterruptedException e)
				{
					logWriter.log("x::Driver::Check SD Card::Thread interrupted! "+e.getMessage());
					e.printStackTrace();
				}
				// SD card should now be mounted
				logWriter.log("After SD card wait thread. sdCardMounted="+(sdCardMounted?"true":"false"));
				
				//Setup registration
    			Reg = new Registration(actLog);
    			
    	    	try//wait here till Reg is OK = OK to send ;) 
    	    	{		
    		    	while(!Reg.regOK())//while not ok
    		    	{
    		    		logWriter.log("Still no SN number from server :(");    		    	
    		    		Thread.sleep(1000*6);//6 seconds(debugging)    		    	
    		    		//Thread.sleep(1000*60*4);//4min	    		
    		    	}
    	    	}
    	    	catch (InterruptedException e) 
    			{	
    	    		logWriter.log("Driver::Check Reg::InterruptedException::"+e.getMessage());
    				//actLog.addMessage(new ErrorMessage(e));
    			}
    	    	///////////////
    	    	
    	    	
    			logWriter.log("Driver::Doing startup work now...");
    			doStartupWork();
    		} // end Run()                        
    	}); // end invokeLater(Runnable())
    } // end doStartupWorkLater
    
    private void doStartupWork()
    {
    	// Get database/vector storage
        actLog = LocalDataAccess.getLocalDataAccessRef();
        logWriter.log("MobileMinder::Driver->Start...");
        
        // For future "Registration" feature 
        //int employerID  = 1;
        //int deviceID    = 2;
        
        //Create variables
        int oneSec        = 1000;
        //int uploadTimer =  1*oneSec;//send update every
        int GPSTimer    = 15*oneSec;//check GPS every
      //  int AppTimer    =  2*oneSec;//check running app every
        
        // Load sub-components
        // new MyServerUpload(actLog, employerID, deviceID, uploadTimer);

        new MyGPSListener (actLog, GPSTimer);
        //new MyAppListener (actLog, AppTimer);            
        new MyMailListener(actLog);
        new MyTextListener(actLog);
        new MyCallListener(actLog);        
    	// Start up connection to the server
    	new Server(actLog).start();
    }
    
	public void powerUp()
    {
	    logWriter.log("Power up...");
	    removeSystemListener(this);
	    doStartupWorkLater();
    }

    public void batteryGood() 
    {
    	logWriter.log("Battery Good...");
    }
    
    public void batteryLow() 
    {
    	logWriter.log("Battery Low...");
    }
    
    public void batteryStatusChange(int arg0)
    {
    	logWriter.log("BatteryStatusChange...");
    }
    
    public void powerOff()
    {
    	logWriter.log("Power off...");
    }

	public void backlightStateChange(boolean on)
	{
		logWriter.log("BacklightStageChange");
		logWriter.log("Backlight "+(on?"ON.":"OFF."));
	}

	public void cradleMismatch(boolean arg0)
	{
    	logWriter.log("cradleMismatch...");
	}

	public void fastReset()
	{
    	logWriter.log("fastReset...");
	}

	public void powerOffRequested(int arg0)
	{
    	logWriter.log("powerOffRequested...");
	}

	public void usbConnectionStateChange(int arg0)
	{
    	logWriter.log("usbConnectionStateChanged...");
	}
}

/*
 * LINKS
 * http://na.blackberry.com/eng/devjournals/resources/journals/jan_2005/api_spotlight_phone.jsp#phonecall_getCallId
 * http://today.java.net/pub/a/today/2009/07/02/blackberry-j2me-development.html
 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800901/How_to_-_Detect_system_availability_on_startup.html?nodeid=1396048&vernum=0
 * http://docs.blackberry.com/en/developers/deliverables/8540/Phone_565538_11.jsp
 * http://www.johnwargo.com/index.php/domino/45-dbja1
 */