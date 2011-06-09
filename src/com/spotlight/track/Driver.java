/**
 * 
 * Driver contains the main entry point of the application, 
 * this is housed within the controller class. 
 * 
 */


package com.spotlight.track;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import net.rim.device.api.system.Application;
 
/**
 * 
 * Controller is called by the Application Manager after it calls the Application.
 *  
 * The controller registers objects with the system to monitor device actions
 *
 */

//class Controller extends Application
public class Driver extends Application
{       // Enable logging
		Debug logWriter = Logger.getInstance();
		//LegacyDataAccess legLog = new innerLegacyDataAccess();
		//LocalDataAccess locLog = new innerLocalDataAccess();
		
		LocalDataReader actLog = LocalDataAccess.getLocalDataAccessRef();
		
		
    	//new way of creating the database
    	LocalDataFactory factory = createOsSpecificDBFactory();
        LocalDataReader actlog = factory.createLocalDataReader();
		
        /**
         * After calling to enterEventDispatcher() the application enters the event-processing loop.
         * 
         *
         */
        public static void main(String[] args)
        {
            //The event thread processes incoming messages and sends them to the listeners.
        	//new Controller().enterEventDispatcher();
        	new Driver().enterEventDispatcher();
        }
     
        //method to check sdk version and return the correct database object
        public static LocalDataFactory createOsSpecificDBFactory()
        {
	    int OS = Tools.getGenOSVersion();//change to your code to check the sdk verson
	    if (OS < 5)
	    {
	    	return new LegacyFactory();
	    }
	    else
	    {
	        return new SQliteFactory();
	    }
	}

/**
 * Initialises the objects that will register themselves with the appropriate event listeners
 */
    //public Controller()
    public Driver()
        {
    	logWriter.log("MobileMinder::Driver->Start...");
    	
    	// For future "Registration" feature 
        int employerID  = 1;
        int deviceID    = 2;
        
        //Create variables
        int oneSec        = 1000;
        int uploadTimer =  1*oneSec;//send update every
        int GPSTimer    = 15*oneSec;//check GPS every
        int AppTimer    =  2*oneSec;//check running app every
        
        // Load sub-components
        // What type should actLog be?
        new MyServerUpload(actLog, employerID, deviceID, uploadTimer);
    	new MyCallListener(actLog);
	    new MyTextListener(actLog);
	    new MyMailListener(actLog);
	    //new MyGPSListener (actLog, GPSTimer);
	    new MyAppListener (actLog, AppTimer);	     
	    new Server(actLog);
        
        
        /*synchronized(Application.getEventLock()){    UiEngine ui = Ui.getUiEngine();
        Screen screen = new Dialog(Dialog.D_OK, "Shirts!!!!!!",
            Dialog.OK,           Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),         Manager.VERTICAL_SCROLL);
        ui.pushGlobalScreen(screen, 1, UiEngine.GLOBAL_QUEUE);
    }*/
        //actLog.removeAction(0);
      
        }	// End of constructor
}

/*
 * LINKS
 * http://na.blackberry.com/eng/devjournals/resources/journals/jan_2005/api_spotlight_phone.jsp#phonecall_getCallId
 * http://today.java.net/pub/a/today/2009/07/02/blackberry-j2me-development.html
 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800901/How_to_-_Detect_system_availability_on_startup.html?nodeid=1396048&vernum=0
 * http://docs.blackberry.com/en/developers/deliverables/8540/Phone_565538_11.jsp
 * http://www.johnwargo.com/index.php/domino/45-dbja1
 */
