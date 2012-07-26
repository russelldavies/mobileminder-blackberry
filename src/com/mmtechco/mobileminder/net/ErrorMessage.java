//#preprocess
package com.mmtechco.mobileminder.net;

import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class ErrorMessage extends Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>Device Time when the error occurred.
	 * <li>Class name in which error occurred.
	 * <li>Package name in which error occurred.
	 * <li>Line number in class in which error occurred.
	 * <li>Type of the error.
	 * <li>Device uptime
	 * </ul>
	 */
	public ErrorMessage(Exception e) {
		this(null, e);
	}

	public ErrorMessage(String message, Exception e) {
		super(Message.ERROR, new String[] {
				ToolsBB.getInstance().getDate(),
				e.getClass().getName(),
				e.getClass().getName(),
				"0",
				e.getMessage(),
				String.valueOf(ToolsBB.getInstance().getUptimeInSec())
		});
		
		if (message == null) {
			Logger.getLogger(e.getClass()).error(e.getMessage());
		} else {
			Logger.getLogger(e.getClass()).error(message + ": " + e.getMessage());
		}
		
		//#ifdef DEBUG
		e.printStackTrace();
		//#endif
	}
}