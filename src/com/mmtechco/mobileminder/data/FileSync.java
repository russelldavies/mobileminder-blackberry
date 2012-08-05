package com.mmtechco.mobileminder.data;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.ui.UiApplication;

import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Synchronizes device files of a certain criteria to the server. Specify the
 * supported file types in {@link FileSync#supportedType(String)}
 */
public class FileSync {
	private static Logger logger = Logger.getLogger(FileSync.class);

	private static final String storeDir = "file:///store/home/user/";
	private static final String sdcardDir = "file:///SDCard/";
	
	private static final FileListener fileListener = new FileListener();

	public static void sync() {
		// Start listening for file events
		UiApplication.getUiApplication().addFileSystemJournalListener(fileListener);
		new Thread() {
			public void run() {
				// Find files sdcard
				if (ToolsBB.fsMounted(FILESYSTEM.SDCARD)) {
					logger.debug("Finding files on sdcard");
					findFiles(sdcardDir);
				}
				// Find files on eMMC
				if (ToolsBB.fsMounted(FILESYSTEM.STORE)) {
					logger.debug("Finding files on eMMC");
					findFiles(storeDir);
				}

				// Upload files to server
				FileLog.upload();

			}
		}.start();
	}

	private static void findFiles(String directory) {
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
						FileLog.addBatch(path);
					}
				}
			} catch (IOException e) {
				ActivityLog.addMessage(new ErrorMessage("Could not open file",
						e));
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
		// Don't look in the /system dir
		if (filePath.startsWith("file:///system/")) {
			return false;
		}
		
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
	
	public static void stop() {
		UiApplication.getUiApplication().removeFileSystemJournalListener(fileListener);
	}
}
