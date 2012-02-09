//#preprocess
package com.mmtechco.util;

import java.util.Date;

import com.mmtechco.mobileminder.ui.DebugScreen;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.util.StringUtilities;

/**
 * Provides a simple logging facility. Can also use the BlackBerry EventLogger.
 * <p>
 * To use console output, use the @param out and @param err methods.
 * <p>
 * To use the device debugging, use the log*Event methods. However, you first
 * need to register the app with EventLog (see EventLog register method) in main
 * app method: EventLogger.register(Logger.GUID, Logger.APP_NAME,
 * EventLogger.VIEWER_STRING);
 * 
 */
public class Logger {
	private static Logger logger;

	private static final String APP_NAME = ApplicationDescriptor
			.currentApplicationDescriptor().getName();
	private static final long GUID = StringUtilities
			.stringHashToLong(Application.getApplication().getClass().getName());

	// Used to format dates into a standard format
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static DebugScreen scr;

	protected Logger() {
	}

	public static Logger getInstance() {
		if (logger == null) {
			logger = new Logger();
		}
		return logger;
	}

	public void log(String tag, String msg) {
		//#ifdef DEBUG
		out(tag, msg);
		if (scr != null) {
			scr.addNewLog(tag + "::" + msg);
		}
		//#else
		logInformationEvent(msg);
		//#endif
	}

	public static void addObserver(DebugScreen screen) {
		scr = screen;
	}

	// These methods are for logging to console. Useful for simulator debugging.
	/**
	 * Log a message to console.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void out(String tag, String msg) {
		System.out.println(setUpMessageString(tag, msg));
	}

	/**
	 * Log an error to console.
	 * 
	 * @param msg
	 *            the message to log
	 * @param t
	 *            the exception to be caught
	 */
	public void err(String tag, String msg, Throwable t) {
		System.err.println(setUpMessageString(tag, msg));
		t.printStackTrace();
	}

	private String setUpMessageString(String tag, String msg) {
		return "***" + APP_NAME + "*** ["
				+ dateFormat.format(new Date()) + "] " + tag + "::" + msg;
	}

	// These methods are for logging to BlackBerry EventLog. Useful for device
	// debugging.
	public void startEventLogger() {
		EventLogger.register(GUID, APP_NAME,
				EventLogger.VIEWER_STRING);
	}

	private static void logEvent(String msg, int level) {
		EventLogger.logEvent(GUID, msg.getBytes(), level);
	}

	public static void logDebugEvent(String msg) {
		logEvent(msg, EventLogger.DEBUG_INFO);
	}

	public static void logInformationEvent(String msg) {
		logEvent(msg, EventLogger.INFORMATION);
	}

	public static void logWarningEvent(String msg) {
		logEvent(msg, EventLogger.WARNING);
	}

	public static void logErrorEvent(String msg) {
		logEvent(msg, EventLogger.ERROR);
	}

	public static void logSevereErrorEvent(String msg) {
		logEvent(msg, EventLogger.SEVERE_ERROR);
	}

	public static void logAlwaysEvent(String msg) {
		logEvent(msg, EventLogger.ALWAYS_LOG);
	}
}
