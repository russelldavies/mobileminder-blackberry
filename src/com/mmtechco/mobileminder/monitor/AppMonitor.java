package com.mmtechco.mobileminder.monitor;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;

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

	private static UiApplication app = UiApplication.getUiApplication();

	int foregroundProcessId = -1;
	String name, moduleName;

	TimerTask checkAppsTask = new TimerTask() {
		public void run() {
			int id = getForegroungProcessID();
			if (id != foregroundProcessId) {
				foregroundProcessId = id;
				getAppNameByProcessId(foregroundProcessId);
				logger.debug("Current running app name is: " + name);
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

class AppMessage extends Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>App name
	 * <li>device date
	 * <li>device uptime
	 * <li>app package name
	 * </ul>
	 */
	public AppMessage(String appName, String packageName) {
		super(Message.APP_USAGE, new String[]{
				appName,
				ToolsBB.getInstance().getDate(),
				String.valueOf(ToolsBB.getInstance().getUptimeInSec()),
				packageName
		});
	}
}