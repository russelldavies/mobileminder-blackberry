package com.mmtechco.mobileminder.util;

import net.rim.device.api.system.Application;
import net.rim.device.api.util.StringUtilities;

public class Constants {
	public static final String APP_NAME = "MobileMinder";
	public static final long GUID = StringUtilities.stringHashToLong(Application.getApplication().getClass().getName());
	
	// Global flag to turn on debugging features
	public static final boolean DEBUG = false;
}
