package com.mmtechco.mobileminder;

import com.mmtechco.mobileminder.data.DBAccess;
import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.UiApplication;

/**
 * Main entry point of the application.
 */
class MobileMinder extends UiApplication implements SystemListener2 {
	private static final String TAG = "App";
	private static MobileMinder app;
	
	public static void main(String[] args) {
		// Start logging
		// Logger.startEventLogger();

		app = new MobileMinder();

		/*
		if (args != null && args.length > 0 && args[0].equals("autostartup")) {
			// App started from autorun on startup
		*/
		
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
				app.doStartupWorkLater();
				//UiApplication.getUiApplication().pushScreen(new InfoScreen());
			}
		/*
		} else {
			// App was started from icon click
			Logger.getInstance().log(TAG, "Started from icon click");
			app.doStartupWorkLater();
			UiApplication.getUiApplication().pushScreen(new InfoScreen());
		}
		*/
			
		// Start event thread
		app.enterEventDispatcher();
	}
	
	// Spawn the controller which takes care of execution of everything else.
	private void doStartupWork() {
		Controller c = new Controller();
		new Thread(c).start();
	}
	
	/**
	 * Perform the start up work on a new Runnable using the invokeLater
	 * construct to ensure that it is executed after the event thread has been
	 * created.
	 */
	private void doStartupWorkLater() {
		invokeLater(new Runnable() {
			public void run() {
				doStartupWork();
			}
		});
	}

	public void powerUp() {
		removeSystemListener(this);
		// Wait 30 seconds for sdcard to mount
		for(int i = 0; i < 30; i++) {
			if (ToolsBB.fsMounted(FILESYSTEM.SDCARD)) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.getInstance().log(TAG, "Started from powerup");
		doStartupWork();
	}

	public void powerOff() {
		DBAccess.close();
	}

	public void batteryLow() {
	}

	public void batteryStatusChange(int status) {
	}

	public void rootChanged(int state, String rootName) {
		// TODO: implement
	}

	public void backlightStateChange(boolean on) {
		// TODO: possibly restart app if crashed
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