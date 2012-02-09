package com.mmtechco.mobileminder.sync;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.ui.UiApplication;

import com.mmtechco.mobileminder.data.FileListener;
import com.mmtechco.mobileminder.data.FileLog;
import com.mmtechco.mobileminder.prototypes.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.FILESYSTEM;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Synchronizes device files of a certain criteria to the server. Specify the
 * supported file types in {@link FileSync#supportedType(String)}
 */
public class FileSync extends Thread implements Controllable {
	private static final String TAG = ToolsBB
			.getSimpleClassName(FileSync.class);

	private static Logger logger = Logger.getInstance();
	private static MMTools tools = ToolsBB.getInstance();

	private static final String storeDir = "file:///store/";
	private static final String sdcardDir = "file:///SDCard/";

	public void run() {
		logger.log(TAG, "Running");
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
		} catch (Exception e) {
			logger.log(TAG, e.toString());
		} finally {
			try {
				fc.close();
			} catch (Exception e) {
				logger.log(TAG, e.toString());
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
			} catch (Exception e) {
				logger.log(TAG, e.toString());
			} finally {
				if (fc != null) {
					try {
						fc.close();
					} catch (Exception e) {
						logger.log(TAG, e.toString());
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
			logger.log(TAG, "Processing Delete File Command...");
			new Thread() {
				public void run() {
					FileConnection fc = null;
					try {
						fc = (FileConnection) Connector.open(path);
						fc.delete();
						// Delete entry from db
						FileLog.delete(path);
					} catch (IOException e) {
						logger.log(TAG, e.toString());
					} finally {
						try {
							if (fc != null) {
								fc.close();
							}
						} catch (Exception e) {
							logger.log(TAG, e.toString());
						}
					}
				}
			}.start();
			return true;
		}
		// Sync files over mobile connection
		if (command.equals(commandMobile)) {
			logger.log(TAG, "Processing Mobile Sync Command...");
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
