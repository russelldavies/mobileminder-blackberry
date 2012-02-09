package com.mmtechco.mobileminder.monitor;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors and registers application based events.
 */
public class AppMonitor extends Thread {
	private static final String TAG = ToolsBB
			.getSimpleClassName(AppMonitor.class);

	// Interval that polling is done, in milliseconds
	private static int interval = 30 * 1000;

	private static final Logger logger = Logger.getInstance();

	int foregroundProcessId = -1;
	String name, moduleName;

	TimerTask checkAppsTask = new TimerTask() {
		public void run() {
			int id = getForegroungProcessID();
			if (id != foregroundProcessId) {
				foregroundProcessId = id;
				getAppNameByProcessId(foregroundProcessId);
				logger.log(TAG, "Current running app name is: " + name);
				ActivityLog.addMessage(new AppMessage(name, moduleName));
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

/**
 * Implements the message interface to hold application information.
 * 
 **/
class AppMessage implements Message {
	private final int type = 5;
	private int upTime = 0;
	private String appName;
	private String fullPackageName;

	public AppMessage(String appName, String packageName) {
		this.appName = appName;
		fullPackageName = packageName;
	}

	public int getType() {
		return type;
	}

	public String getTime() {
		return "";
	}

	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + "0"
				+ getType() + Tools.ServerQueryStringSeparator + appName
				+ Tools.ServerQueryStringSeparator + ToolsBB.getInstance().getDate()
				+ Tools.ServerQueryStringSeparator + upTime
				+ Tools.ServerQueryStringSeparator + fullPackageName;
	}
}