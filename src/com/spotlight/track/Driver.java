/**
 * 
 * Driver contains the main entry point of the application, this is housed within the controller class. 
 * 
 * 
 */


package com.spotlight.track;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Date;
import java.util.Vector;
  
import javax.microedition.io.Connector;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Persistable;


import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.blackberry.api.sms.OutboundMessageListener;

import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;
 
/**
 * 
 * Controller is called by the Application Manager after it calls the Application.
 *  
 * the controller registers objects with the system to monitor device actions
 *
 */

class Controller extends Application
{       
    
        /**
         * After calling to enterEventDispatcher() the application enters the event-processing loop.
         * 
         *
         */
        public static void main(String[] args)
    {
                //The event thread processes incoming messages and sends them to the listeners.
        new Controller().enterEventDispatcher();
    }
     
/**
 * Initialises the objects that will register themselves with the appropriate event listeners
 */
    public Controller()
    {
        int employerID  = 1;
        int deviceID    = 2;
        int oneSec        = 1000;
        int uploadTimer =  1*oneSec;//send update every
        int GPSTimer    = 15*oneSec;//check GPS every
        int AppTimer    =  2*oneSec;//check running app every
        
        LocalDataAccess actLog = new LocalDataAccess();
        actLog.addAction(action.TYPE_IDEL,"Starting Controler");
        
        /*synchronized(Application.getEventLock()){    UiEngine ui = Ui.getUiEngine();
        Screen screen = new Dialog(Dialog.D_OK, "Shirts!!!!!!",
            Dialog.OK,           Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),         Manager.VERTICAL_SCROLL);
        ui.pushGlobalScreen(screen, 1, UiEngine.GLOBAL_QUEUE);
    }*/
        //actLog.removeAction(0);
        new MyCallListener(actLog);
        new MyTextListener(actLog);
        new MyMailListener(actLog);
        new MyGPSListener (actLog, GPSTimer);
        new MyAppListener (actLog, AppTimer);
        new MyServerUpload(actLog, employerID, deviceID, uploadTimer);
    }
    
}



/*
class toast implements Runnable
{
        
        private String Message;
        
        toast(String inputMessage)
        {
                Message = inputMessage;
        }
        
    public void run() 
    {
        Status.show(Message);
    }
}
*/

/*
 * LINKS
 * http://na.blackberry.com/eng/devjournals/resources/journals/jan_2005/api_spotlight_phone.jsp#phonecall_getCallId
 * http://today.java.net/pub/a/today/2009/07/02/blackberry-j2me-development.html
 * http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800901/How_to_-_Detect_system_availability_on_startup.html?nodeid=1396048&vernum=0
 * http://docs.blackberry.com/en/developers/deliverables/8540/Phone_565538_11.jsp
 * http://www.johnwargo.com/index.php/domino/45-dbja1
 */
