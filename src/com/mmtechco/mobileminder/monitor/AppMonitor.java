package com.mmtechco.mobileminder.monitor;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.ui.BrowserScreen;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors and registers application based events.
 */
public class AppMonitor extends Thread {
	private static final String TAG = ToolsBB
			.getSimpleClassName(AppMonitor.class);

	// Interval that polling is done, in milliseconds
	private static int interval = 1 * 1000;

	private static UiApplication app = UiApplication.getUiApplication();

	int foregroundProcessId = -1;
	String name, moduleName;

	TimerTask checkAppsTask = new TimerTask() {
		public void run() {
			int id = getForegroungProcessID();
			if (id != foregroundProcessId) {
				foregroundProcessId = id;
				getAppNameByProcessId(foregroundProcessId);
				Logger.log(TAG, "Current running app name is: " + name);
				ActivityLog.addMessage(new AppMessage(name, moduleName));

				// If system browser is open, close it and start custom browser
				if (name.equals("Browser")) {
					// Bring up menu
					EventInjector.invokeEvent(new EventInjector.KeyCodeEvent(
							EventInjector.KeyCodeEvent.KEY_DOWN,
							(char) Keypad.KEY_MENU, 0));
					// Cycle down menu
					for (int i = 0; i < 20; i++) {
						EventInjector
								.invokeEvent(new EventInjector.NavigationEvent(
										EventInjector.NavigationEvent.NAVIGATION_MOVEMENT,
										0, 1, 0));
					}
					// Click on menu item
					EventInjector.invokeEvent(new EventInjector.KeyCodeEvent(
							EventInjector.KeyCodeEvent.KEY_DOWN,
							(char) Keypad.KEY_ENTER, 0));
					
					// Start custom browser
					app.invokeAndWait(new Runnable() {
						public void run() {
							app.pushScreen(new BrowserScreen());
						}
					});
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
		return Registration.getRegID() + Server.separator + "0"
				+ getType() + Server.separator + appName
				+ Server.separator
				+ ToolsBB.getInstance().getDate()
				+ Server.separator + upTime
				+ Server.separator + fullPackageName;
	}
}