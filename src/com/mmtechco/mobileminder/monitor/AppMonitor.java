package com.mmtechco.mobileminder.monitor;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.ui.BrowserScreen;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors and registers application based events.
 */
public class AppMonitor extends Thread {
	private static Logger logger = Logger.getLogger(AppMonitor.class);

	// Interval that polling is done, in milliseconds
	private static int interval = 1 * 1000;

	int foregroundProcessId = -1;
	String name, moduleName;
	
	AppMessage message;

	TimerTask checkAppsTask = new TimerTask() {
		public void run() {
			int id = getForegroungProcessID();
			if (id != foregroundProcessId) {
				foregroundProcessId = id;
				getAppNameByProcessId(foregroundProcessId);
				logger.debug("Current running app name is: " + name);
				
				if (message != null) {
					message.finished();
				}
				message = new AppMessage(name, moduleName);

				// If system browser is open, close it and start custom browser
				if (name.equals("Browser")) {
					BrowserScreen.display();
				}
			}
		}
	};

	public AppMonitor() {
		new Timer().scheduleAtFixedRate(checkAppsTask, 0, interval);
	}

	private int getForegroungProcessID() {
		return ApplicationManager.getApplicationManager()
				.getForegroundProcessId();
	}

	private void getAppNameByProcessId(int id) {
		ApplicationManager appMan = ApplicationManager.getApplicationManager();
		ApplicationDescriptor appDes[] = appMan.getVisibleApplications();
		for (int i = 0; i < appDes.length; i++) {
			if (appMan.getProcessId(appDes[i]) == id) {
				name = appDes[i].getName();
				moduleName = appDes[i].getModuleName();
				break;
			}
		}
	}
}

class AppMessage extends Message {
	private Date startTime;
	private String packageName;
	
	/**
	 * Message format:
	 * <ul>
	 * <li>app name
	 * <li>device date
	 * <li>app duration
	 * <li>app package name
	 * </ul>
	 */
	public AppMessage(String appName, String packageName) {
		super(Message.APP_USAGE);
		add(appName);
		add(ToolsBB.getInstance().getDate());
		this.packageName = packageName;
		startTime = new Date();
	}
	
	public void finished() {
		add(String.valueOf((int) (new Date().getTime() - startTime.getTime()) / 1000));
		add(packageName);
		
		ActivityLog.addMessage(this);
	}
}