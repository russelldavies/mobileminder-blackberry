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
		super(Message.ERROR, new String[] {
				ToolsBB.getInstance().getDate(),
				e.getClass().getName(),
				e.getClass().getName(),
				"0",
				e.getMessage(),
				String.valueOf(ToolsBB.getInstance().getUptimeInSec())
		});
		
		Logger.log(e.getClass().getName(), e.getMessage());
		
		//#ifdef DEBUG
		e.printStackTrace();
		//#endif
	}
}