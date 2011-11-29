package com.mmtechco.mobileminder;

import java.io.IOException;
import com.mmtechco.mobileminder.contacts.ContactPic;
import com.mmtechco.mobileminder.control.Commander;
import com.mmtechco.mobileminder.data.DBFactory;
import com.mmtechco.mobileminder.monitor.*;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.sync.CallSync;
import com.mmtechco.mobileminder.util.Logger;

/**
 * Initializes event listeners for the system and starts server and
 * registration.
 * 
 */
public class Controller implements Runnable {
	private static final String TAG = "Controller";

	private LocalDataWriter actLog;
	private Registration reg;
	private Logger logger = Logger.getInstance();
	
	// Timer values
	private int locTime = 29000;
	private int appTime = 31000;
	

	public void run() {
		logger.log(TAG, "Starting registration");
		reg = new Registration();
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
			// TODO: pop dialog to user
		}
		actLog.open();

		// Start monitors
		logger.log(TAG, "Starting monitors...");
		new AppMonitor(actLog, appTime);
		new CallSync(new Server(actLog)).start();
		new CallMonitor(actLog);
		new LocationMonitor(actLog, locTime);
		new MailMonitor(actLog);
		
		Controllable[] components = new Controllable[3];
		components[0] = new SMSMonitor(actLog);
		components[1] = new CallMonitor(actLog);
		components[2] = new ContactPic(actLog);
		new Commander(actLog, components).start();
		
		// Monitor activity log
		new Server(actLog).start();
	}
}
