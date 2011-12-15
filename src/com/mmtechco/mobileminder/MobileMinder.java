package com.mmtechco.mobileminder;

import com.mmtechco.mobileminder.contacts.ContactPic;
import com.mmtechco.mobileminder.data.DbFactory;
import com.mmtechco.mobileminder.data.LogDb;
import com.mmtechco.mobileminder.monitor.*;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;
import com.mmtechco.mobileminder.sync.CallSync;
import com.mmtechco.mobileminder.sync.FileSync;
import com.mmtechco.mobileminder.util.Constants;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.StorageException;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

/**
 * Main entry point of the application.
 */
class MobileMinder extends UiApplication implements SystemListener2 {
	private static final String TAG = "App";
	public static ResourceBundle r = ResourceBundle.getBundle(
			MobileMinderResource.BUNDLE_ID, MobileMinderResource.BUNDLE_NAME);
	
	private Logger logger = Logger.getInstance();

	private LogDb actLog;
	private InfoScreen infoscreen;

	/**
	 * Entry point for application
	 * 
	 * @param args
	 *            Alternate entry point arguments.
	 */
	public static void main(String[] args) {
		// Start logging
		// TODO: implement
		// Logger.startEventLogger();

		MobileMinder app = new MobileMinder();

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
			app.initializeLater();
		}

		// Setup listener for removal of app. This needs to be set here before
		// the app enters the event dispatcher.
		CodeModuleManager.addListener(app, new UninstallMonitor());

		// Start event thread
		app.enterEventDispatcher();
	}

	private void initialize() {
		if (Constants.DEBUG) {
			pushScreen(new DebugScreen());
		} else {
			infoscreen = new InfoScreen();
			pushScreen(infoscreen);
		}

		logger.log(TAG, "Starting registration");
		Registration reg = new Registration();
		reg.start();
	}

	/**
	 * Start components
	 */
	public void startComponents() {
		if (!Constants.DEBUG) {
			// Register application indicator
			//infoscreen.registerIndicator();
		}

		// Open the database
		try {
			actLog = DbFactory.getLocalDataWriter();
			actLog.open();
		} catch (StorageException e) {
			logger.log(TAG, e.getMessage() + " Alerting user");
			Dialog.alert(r.getString(MobileMinderResource.i18n_StorageError));
			return;
		} catch (DatabaseException e) {
			e.printStackTrace();
			logger.log(TAG, "Filesystem could not access DB");
			return;
		}

		// Start call sync. Note that there is no faculty to access existing SMS
		// messages stored on the device.
		new Thread(new CallSync()).start();

		// Start monitors
		logger.log(TAG, "Starting monitors...");
		new AppMonitor(actLog);
		new LocationMonitor(actLog);
		new MailMonitor(actLog);
		new CallMonitor(actLog);
		new SMSMonitor(actLog);

		Controllable[] components = new Controllable[2];
		components[0] = new ContactPic(actLog);
		FileSync mediasync = new FileSync(actLog);
		components[1] = mediasync;
		new Commander(actLog, components).start();
		mediasync.start();

		// Monitor activity log
		new Server(actLog).start();
	}

	/**
	 * Perform the start up work on a new Runnable using the invokeLater
	 * construct to ensure that it is executed after the event thread has been
	 * created.
	 */
	private void initializeLater() {
		invokeLater(new Runnable() {
			public void run() {
				initialize();
			}
		});
	}

	public void powerUp() {
		Logger.getInstance().log(TAG, "Started from powerup");

		removeSystemListener(this);
		// Wait up to 30 seconds for sdcard to mount
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
		initialize();
	}

	public void powerOff() {
		try {
			actLog.close();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not close db");
			System.exit(1);
		}
		System.exit(0);
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