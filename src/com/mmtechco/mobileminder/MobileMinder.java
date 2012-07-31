//#preprocess
package com.mmtechco.mobileminder;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.UiApplication;

import com.mmtechco.mobileminder.command.Commander;
import com.mmtechco.mobileminder.command.ContactPic;
import com.mmtechco.mobileminder.command.EmergencyNumbers;
import com.mmtechco.mobileminder.command.FileControl;
import com.mmtechco.mobileminder.data.FileSync;
import com.mmtechco.mobileminder.monitor.AppMonitor;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.monitor.LocationMonitor;
import com.mmtechco.mobileminder.monitor.SMSMonitor;
import com.mmtechco.mobileminder.monitor.UninstallMonitor;
import com.mmtechco.mobileminder.ui.DebugScreen;
import com.mmtechco.mobileminder.ui.InfoScreen;
import com.mmtechco.util.Logger;

/**
 * Main entry point of the application.
 */
class MobileMinder extends UiApplication implements SystemListener2, GlobalEventListener {
	public static ResourceBundle r = ResourceBundle.getBundle(
			MobileMinderResource.BUNDLE_ID, MobileMinderResource.BUNDLE_NAME);
	private Logger logger = Logger.getLogger(MobileMinder.class);

	private InfoScreen infoscreen;

	/**
	 * Entry point for application
	 * 
	 * @param args
	 *            Alternate entry point arguments.
	 */
	public static void main(String[] args) {
		final MobileMinder app = new MobileMinder();
		
		// Listen for registration events
		app.addGlobalEventListener(app);
		//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
		// Setup listener for removal of app. This needs to be set here before
		// the app enters the event dispatcher.
		CodeModuleManager.addListener(app, new UninstallMonitor());
		//#endif
		
		// If system startup is still in progress when this
		// application is run.
		if (ApplicationManager.getApplicationManager().inStartup()) {
			// Add a system listener to detect when system is ready and
			// available.
			app.addSystemListener(app);
		} else {
			// System is already ready and available so perform start up
			// work now. Note that this work must be completed using
			// invokeLater because the application has not yet entered the
			// event dispatcher.
			app.invokeLater(new Runnable() {
				public void run() {
					app.initialize();
				}
			});
		}

		// Start event thread
		app.enterEventDispatcher();

		// Register for logging
		EventLogger.register(Logger.GUID, Logger.APP_NAME,
				EventLogger.VIEWER_STRING);
		// #ifdef DEBUG
		EventLogger.clearLog();
		EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
		// #endif
	}

	private void initialize() {
		//#ifdef DEBUG
		pushScreen(new DebugScreen());
		//#else
		infoscreen = new InfoScreen();
		pushScreen(infoscreen);
		//#endif

		// This needs to be in a thread as it performs network IO and would run
		// on the event queue and freeze the interface. Subsequent calls to it
		// use a separate timer thread
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				new Thread() {
					public void run() {
						Registration.checkStatus();
					}
				}.start();
			}
		});
	}

	public void eventOccurred(long guid, int data0, int data1, Object object0,
			Object object1) {
		if (guid == Registration.ID) {
			logger.debug("Received event to start components");
			startComponents();
		}
	}
	
	/**
	 * Start components
	 */
	public void startComponents() {
		logger.debug("Starting components");
		
		//#ifndef VER_4.5.0
		// Register application indicator
		//infoscreen.registerIndicator();
		//#endif

		// Start call sync. Note that there is no faculty to access existing SMS
		// messages stored on the device.
		CallSync.sync();
		FileSync.sync();

		// Start monitors
		logger.debug("Starting monitors...");
		new AppMonitor();
		//new MailMonitor();
		new CallMonitor();
		new SMSMonitor();
		LocationMonitor.start();
		
		logger.debug("Adding components to Commander");
		Commander.addComponent(new ContactPic());
		Commander.addComponent(new FileControl());
		Commander.addComponent(new EmergencyNumbers());
		Commander.startProcessing();
	}

	public void powerUp() {
		logger.debug("Started from powerup");
		removeSystemListener(this);
		// Wait for 30 seconds so OS network is initialized
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			logger.warn(e.getMessage());
		}
		initialize();
	}

	public void powerOff() {
	}

	public void rootChanged(int state, String rootName) {
		// TODO: implement
	}

	public void backlightStateChange(boolean on) {
		// TODO: possibly restart app if crashed
	}

	public void batteryLow() {
	}

	public void batteryStatusChange(int status) {
	}

	public void cradleMismatch(boolean mismatch) {
	}

	public void fastReset() {
	}

	public void powerOffRequested(int reason) {
	}

	public void usbConnectionStateChange(int state) {
	}

	public void batteryGood() {
	}
}