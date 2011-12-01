package com.mmtechco.mobileminder;

import java.io.IOException;

import net.rim.device.api.database.DatabaseIOException;

import com.mmtechco.mobileminder.contacts.ContactPic;
import com.mmtechco.mobileminder.control.Commander;
import com.mmtechco.mobileminder.data.DBFactory;
import com.mmtechco.mobileminder.monitor.*;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.sync.CallSync;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Initializes event listeners for the system and starts server and
 * registration.
 * 
 */
public class Controller implements Runnable {
	private static final String TAG = ToolsBB.getSimpleClassName(Controller.class);

	private LocalDataWriter actLog;
	private Registration reg;
	private Logger logger = Logger.getInstance();
	
	// Timer values
	private int locTime = 29000;
	private int appTime = 31000;
	

	public void run() {
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
			logger.log(TAG, "Device has no storage that is can be written to by DB. Alerting user.");
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

		// Start sync
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
		//components[3] = new MediaSync(actLog);
		new Commander(actLog, components).start();
		
		// Monitor activity log
		new Server(actLog).start();
	}
}
