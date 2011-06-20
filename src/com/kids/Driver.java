/**
 * 
 * Driver contains the main entry point of the application, 
 * this is housed within the controller class. 
 * 
 */

package com.kids;

import javax.microedition.amms.Module;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.system.SystemListener2;
 
/**
 * 
 * Controller is called by the Application Manager after it calls the Application.
 *  
 * The controller registers objects with the system to monitor device actions
 *
 */

//class Controller extends Application
public class Driver extends Application implements SystemListener2
{       // Enable logging
        static Debug logWriter = Logger.getInstance();
                
        LocalDataReader actLog ;//= LocalDataAccess.getLocalDataAccessRef();
                
        //new way of creating the database
        //LocalDataFactory factory = createOsSpecificDBFactory();
       // LocalDataReader actlog = factory.createLocalDataReader();
                
        /**
         * After calling to enterEventDispatcher() the application enters the event-processing loop.
         * 
         *
         */
        public static void main(String[] args)
        {
            //How to create proper startup apps:
            //http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800332/832062/How_To_-_Write_safe_initialization_code.html?nodeid=1487426&vernum=0
            Driver appInstance = new Driver();
                
                
            // If system startup is still in progress when this
            // application is run.
            if (ApplicationManager.getApplicationManager().inStartup())
                {
                logWriter.log("Still starting up");
                appInstance.addSystemListener(appInstance);
            }
            else
                {
                logWriter.log("Fully booted up");
                appInstance.doStartupWorkLater();
            }
            //The event thread processes incoming messages and sends them to the listeners.
                //new Controller().enterEventDispatcher();
            appInstance.enterEventDispatcher();
        }
       
/**
 * Initialises the objects that will register themselves with the appropriate event listeners
 */
    //public Controller()
    Driver()
        { }

    
    private void doStartupWorkLater()
    {
    	invokeLater(new Runnable()
    	{
    		public void run()
    		{
    			logWriter.log("Doing startup work now...");
    			doStartupWork();
    		}
                        
    	});
    }
    
    private void doStartupWork()
    {
        actLog = LocalDataAccess.getLocalDataAccessRef();
        logWriter.log("MobileMinder::Driver->Start456...");
        
        // For future "Registration" feature 
        //int employerID  = 1;
        //int deviceID    = 2;
        
        //Create variables
      //  int oneSec        = 1000;
        //int uploadTimer =  1*oneSec;//send update every
      //  int GPSTimer    = 15*oneSec;//check GPS every
      //  int AppTimer    =  2*oneSec;//check running app every
        
        // Load sub-components
        // What type should actLog be?
        // new MyServerUpload(actLog, employerID, deviceID, uploadTimer);
        logWriter.log("MobileMinder::Before loading components...");

        new Server(actLog);
        //new MyMailListener(actLog);
        //new MyTextListener(actLog);
        //new MyGPSListener (actLog, GPSTimer);
        //new MyAppListener (actLog, AppTimer);            
        new MyCallListener(actLog);
        logWriter.log("MobileMinder::After loading components...");

        
        /*synchronized(Application.getEventLock()){    UiEngine ui = Ui.getUiEngine();
        Screen screen = new Dialog(Dialog.D_OK, "Shirts!!!!!!",
            Dialog.OK,           Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),         Manager.VERTICAL_SCROLL);
        ui.pushGlobalScreen(screen, 1, UiEngine.GLOBAL_QUEUE);
    }*/ 
        //actLog.removeAction(0);
    }
    

    public void powerUp()
    {
    logWriter.log("Power up...");

    removeSystemListener(this);
    doStartupWork();                
    }

    public void batteryGood() 
    {
        // TODO Auto-generated method stub
    	logWriter.log("Battery Good...");
    }
    
    public void batteryLow() 
    {
        // TODO Auto-generated method stub
    	logWriter.log("Battery Low...");
    }
    
    public void batteryStatusChange(int arg0)
    {
        // TODO Auto-generated method stub
    	logWriter.log("BatteryStatusChange...");
    }
    
    public void powerOff()
    {
        // TODO Auto-generated method stub
    	logWriter.log("Power off...");
    }

	public void backlightStateChange(boolean on) {
		// TODO Auto-generated method stub
		logWriter.log("BacklightStageChange");
	    if(on)
	    {
	    	logWriter.log("Backlight ON.");// Starting app...");
	    	//doStartupWork();                
	    }
	}

	public void cradleMismatch(boolean arg0) {
		// TODO Auto-generated method stub
    	logWriter.log("cradleMismatch...");
	}

	public void fastReset() {
		// TODO Auto-generated method stub
    	logWriter.log("fastReset...");
	}

	public void powerOffRequested(int arg0) {
		// TODO Auto-generated method stub
    	logWriter.log("powerOffRequested...");
	}

	public void usbConnectionStateChange(int arg0) {
		// TODO Auto-generated method stub
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