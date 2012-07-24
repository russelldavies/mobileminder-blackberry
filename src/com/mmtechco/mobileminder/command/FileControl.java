package com.mmtechco.mobileminder.command;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.data.FileLog;
import com.mmtechco.mobileminder.data.FileSync;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

public class FileControl implements Controllable {
	private static Logger logger = Logger.getLogger(FileSync.class);
	private static MMTools tools = ToolsBB.getInstance();
	
	public boolean processCommand(final String[] inputArgs) {
		final String commandDelete = "del";
		final String commandMobile = "mobile";
		final String command = inputArgs[0].toLowerCase();
		// Decode UTF16 characters
		final String path = tools.safeRangeTextUTF(inputArgs[1]);

		// Delete file
		if (command.equals(commandDelete)) {
			logger.debug("Processing Delete File Command...");
			new Thread() {
				public void run() {
					FileConnection fc = null;
					try {
						fc = (FileConnection) Connector.open(path);
						fc.delete();
						// Delete entry from db
						FileLog.delete(path);
					} catch (IOException e) {
						ActivityLog.addMessage(new ErrorMessage("Could not delete file", e));
					} finally {
						try {
							if (fc != null) {
								fc.close();
							}
						} catch (IOException e) {
							logger.error(e.toString());
						}
					}
				}
			}.start();
			return true;
		}
		// Sync files over mobile connection
		if (command.equals(commandMobile)) {
			logger.debug("Processing Mobile Sync Command...");
			FileLog.mobileSync = (path.equals("1") ? true : false);
			return true;
		}
		return false;
	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.FILES) {
			return true;
		} else {
			return false;
		}
	}

}
