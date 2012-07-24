package com.mmtechco.mobileminder.sync;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.ui.UiApplication;

import com.mmtechco.mobileminder.command.COMMAND_TARGETS;
import com.mmtechco.mobileminder.command.Controllable;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.data.FILESYSTEM;
import com.mmtechco.mobileminder.data.FileListener;
import com.mmtechco.mobileminder.data.FileLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

/**
 * Synchronizes device files of a certain criteria to the server. Specify the
 * supported file types in {@link FileSync#supportedType(String)}
 */
public class FileSync extends Thread implements Controllable {
	private static Logger logger = Logger.getLogger(FileSync.class);
	private static MMTools tools = ToolsBB.getInstance();

	private static final String storeDir = "file:///store/";
	private static final String sdcardDir = "file:///SDCard/";
	
	public FileSync() {
		this.start();
	}

	public void run() {
		logger.debug("Running");
		// Find files on eMMC
		if (ToolsBB.fsMounted(FILESYSTEM.STORE)) {
			findFiles(storeDir);
		}
		// Find files sdcard
		if (ToolsBB.fsMounted(FILESYSTEM.SDCARD)) {
			findFiles(sdcardDir);
		}

		// Start listening for file events
		UiApplication.getUiApplication().addFileSystemJournalListener(
				new FileListener());

		// Upload files to server
		FileLog.upload();
	}

	public void findFiles(String directory) {
		FileConnection fc = null;
		Enumeration dirEnum = null;

		try {
			fc = (FileConnection) Connector.open(directory);
			dirEnum = fc.list();
		} catch (IOException e) {
			ActivityLog.addMessage(new ErrorMessage("Could not open file", e));
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}

		while (dirEnum.hasMoreElements()) {
			fc = null;
			String file = (String) dirEnum.nextElement();
			try {
				String path = directory + file;
				fc = (FileConnection) Connector.open(path);
				if (fc.isDirectory()) {
					// Recurse into next subdirectory
					findFiles(path);
				} else {
					// Only add supported filetypes
					if (supportedType(path)) {
						// Add file but don't commit yet
						FileLog.add(path, false);
					}
				}
			} catch (IOException e) {
				ActivityLog.addMessage(new ErrorMessage("Could not open file", e));
			} finally {
				if (fc != null) {
					try {
						fc.close();
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}
		// Since many files may have been added do a batch commit
		FileLog.commit();
	}

	public static boolean supportedType(String filePath) {
		boolean supported = false;
		String contentType = MIMETypeAssociations.getMIMEType(filePath);
		// Normalize and strip off parameters
		String normalizedContentType = MIMETypeAssociations
				.getNormalizedType(contentType);
		switch (MIMETypeAssociations
				.getMediaTypeFromMIMEType(normalizedContentType)) {
		case MIMETypeAssociations.MEDIA_TYPE_IMAGE:
			// case MIMETypeAssociations.MEDIA_TYPE_AUDIO:
			// case MIMETypeAssociations.MEDIA_TYPE_VIDEO:
			supported = true;
			break;
		}
		return supported;
	}

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
