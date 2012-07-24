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

	private static final String TYPE_FILE = "FILE";
	private static final String TYPE_MOBILE = "MOBILE";
	private static final String VERB_DEL = "DEL";

	public boolean processCommand(final String[] args) {
		logger.info("Processing file command");
		try {
			String type = args[0];
			if (type.equalsIgnoreCase(TYPE_FILE)) {
				String verb = args[1];
				// Decode UTF16 characters
				final String path = tools.safeRangeTextUTF(args[2]);

				// Delete file
				if (verb.equalsIgnoreCase(VERB_DEL)) {
					logger.debug("Processing Delete File Command...");
					FileConnection fc = null;
					try {
						fc = (FileConnection) Connector.open(path);
						fc.delete();
						// Delete entry from db
						FileLog.delete(path);
					} catch (IOException e) {
						ActivityLog.addMessage(new ErrorMessage(
								"Could not delete file", e));
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
				return true;
			} else if (type.equalsIgnoreCase(TYPE_MOBILE)) {
				// Sync files over mobile connection
				logger.info("Processing Mobile Sync Command...");
				String boolVal = args[1];
				FileLog.mobileSync = (boolVal.equalsIgnoreCase("true")) ? true
						: false;
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not parse command args", e));
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not process emergency number list", e));
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
