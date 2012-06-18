//#preprocess
package com.mmtechco.util;

import java.util.Date;
import java.util.Hashtable;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.ui.DebugScreen;

public class Logger {
	public static final String APP_NAME = ApplicationDescriptor
			.currentApplicationDescriptor().getName();
	public static final long GUID = StringUtilities
			.stringHashToLong(Application.getApplication().getClass().getName());
	// Used to format dates into a standard format
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	
	private static Hashtable loggers = new Hashtable();
	private static DebugScreen scr;
	
	private String tag;
	
	protected Logger(Class clientClass) {
		tag = ToolsBB.getSimpleClassName(clientClass);
	}

	public static synchronized Logger getLogger(Class clientClass) {
		Logger logger = (Logger)loggers.get(clientClass);
		if (logger == null) {
			logger = new Logger(clientClass);
			loggers.put(clientClass, logger);
		}
		return logger;
	}
	
	public void error(String msg) {
		logEvent(msg, EventLogger.ERROR);
	}
	
	public void warn(String msg) {
		logEvent(msg, EventLogger.WARNING);
	}
	
	public void info(String msg) {
		logEvent(msg, EventLogger.INFORMATION);
	}
	
	public void debug(String msg) {
		logEvent(msg, EventLogger.DEBUG_INFO);
	}
	
	private void logEvent(String msg, int level) {
		EventLogger.logEvent(GUID, msg.getBytes(), level);
		//#ifdef DEBUG
		System.out.println(formatConsole(msg));
		if (scr != null) {
			scr.addNewLog(tag + "::" + msg);
		}
		//#endif
	}
	
	private String formatConsole(String msg) {
		return "***" + APP_NAME + "*** ["
				+ dateFormat.format(new Date()) + "] " + tag + "::" + msg;
	}
	
	public static void addObserver(DebugScreen screen) {
		scr = screen;
	}
}
