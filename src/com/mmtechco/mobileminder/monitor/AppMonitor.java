package com.mmtechco.mobileminder.monitor;

import java.util.Date;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Monitors and registers application based events.
 */
public class AppMonitor extends Thread {
	private static final String TAG = ToolsBB.getSimpleClassName(AppMonitor.class);
	
	private static final Logger logger = Logger.getInstance();
	private LocalDataWriter actLog;
	private int interval;

	/**
	 * The AppListener constructor initialise the action store location and the
	 * interval value. Calls the start() method on the thread which invokes the
	 * run() method of the thread.
	 * 
	 * @param inputAccess
	 *            log of actions
	 * @param inputAppTimer
	 *            interval value
	 */
	public AppMonitor(LocalDataWriter actLog, int inputAppTimer) {
		this.actLog = actLog;
		interval = inputAppTimer;
		this.start();
	}

	/**
	 * 
	 * The Application Listener class stores the application currently running
	 * on the device at specific intervals in the action log in the form of an
	 * action of type app.
	 * <p>
	 * The inherited run method is called when the constructor starts, the
	 * function obtains the id of the application running in the foreground on
	 * the device. It compares the id to the application heap. The application
	 * heap holds the application that have been running on the device stored on
	 * the devices memory This consists of the application id's with the
	 * corresponding name of the application. The application heap is searched
	 * using the foreground application id to find the name of the application.
	 * This is then stored to the action log as an action of type App.
	 * 
	 */
	public void run() {
		logger.log(TAG, "Started");
		try {
			int lastProcessId = 0;
			ApplicationManager manager = ApplicationManager
					.getApplicationManager();
			// Date StartTimer = new Date();
			String lastAppName = "BootUp Device";
			AppMessage appMessage = new AppMessage();
			ApplicationDescriptor runningApps[];

			while (true) {
				sleep(interval);
				// Store a list of running apps
				runningApps = manager.getVisibleApplications();

				// If a new app is detected (by comparing current processID with
				// processID from last loop)
				if (manager.getForegroundProcessId() != lastProcessId) {
					// Make this processID the current working ID
					lastProcessId = manager.getForegroundProcessId();
					// Search through the list of running apps for the app that
					// matches this process ID
					for (int count = 0; runningApps.length > count; count++) {
						// When we find a match....
						if (manager.getProcessId(runningApps[count]) == lastProcessId) {
							lastAppName = runningApps[count].getName();
							logger.log(TAG, "Current running app name is: "
									+ lastAppName);
							// Add to log
							appMessage.clearData();
							appMessage.setMessage(lastAppName,
									runningApps[count].getModuleName());
							// ...and then to the database
							actLog.addMessage(appMessage);
							break;
						}
					}
				}
			}
		}
		catch (InterruptedException e) {
			logger.log(TAG, "InterruptedException::" + e.getMessage());
		}
	}
}

/**
 * Implements the message interface to hold application information.
 * 
 **/
class AppMessage implements Message {
	private final int type = 5;
	private int upTime;
	private String launchTime;
	private String appName;
	private String fullPackageName;
	private Date startTime;
	private MMTools tools = ToolsBB.getInstance();

	public AppMessage() {
		clearData();
	}

	/**
	 * This method adds the application information to the application message
	 * object
	 * 
	 * @param _appName
	 *            application name
	 * @param _packageName
	 *            application package name
	 */
	public void setMessage(String appName, String packageName) {
		this.appName = appName;
		launchTime = tools.getDate();
		startTime = new Date();
		fullPackageName = packageName;
	}

	/**
	 * This method calculates and records the duration of the application
	 */
	public void setEndDuration() {
		upTime = (int) (new Date().getTime() - startTime.getTime()) / 1000;
	}

	/**
	 * This method removes the current data in the message and initialises the
	 * parameters.
	 * 
	 */
	public void clearData() {
		upTime = 0;
		appName = "";
		fullPackageName = "";
		launchTime = "";
		startTime = null;
	}

	public int getType() {
		return type;
	}

	public String getTime() {
		return launchTime;
	}

	/**
	 * Retrieves the message formatted in to a single string value.
	 * <p>
	 * App message consists of:
	 * <ul>
	 * <li>Registration Serial number.
	 * <li>Type of App message which is '05' (two digit number).
	 * <li>Application Name.
	 * <li>Launching time of application.
	 * <li>Application Up-Time.
	 * <li>Application Full Peckage Name.
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return
				Registration.getRegID() +
				Tools.ServerQueryStringSeparator +
				"0" +
				getType() +
				Tools.ServerQueryStringSeparator +
				appName +
				Tools.ServerQueryStringSeparator +
				getTime() +
				Tools.ServerQueryStringSeparator +
				upTime +
				Tools.ServerQueryStringSeparator +
				fullPackageName;
	}
}