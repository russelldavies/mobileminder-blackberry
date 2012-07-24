package com.mmtechco.mobileminder.data;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.crypto.DigestInputStream;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.ObjectGroup;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

public class FileLog {
	public static final long ID = StringUtilities
			.stringHashToLong(FileLog.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector files;
	
	private static Logger logger = Logger.getLogger(FileLog.class);

	public static boolean mobileSync = false;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			files = new ContentProtectedVector();
			store.setContents(files);
		}
		files = (ContentProtectedVector) store.getContents();
	}

	/**
	 * Add file to log committing the file to the persistent store immediately.
	 * If the file already exists it will not be added.
	 * 
	 * @param path
	 *            Fully qualified path to the file
	 */
	public synchronized static void add(String path) {
		add(path, true);
	}
	
	public synchronized static void addBatch(String path) {
		add(path, false);
	}

	/**
	 * Add a file to the log storing the file to the persistent store depending
	 * on the commit flag
	 * 
	 * @param path
	 *            Fully qualified path to the file
	 * @param commit
	 *            Indicates whether to commit to the store. True commits, if
	 *            false is passed then the commit should be handled by the
	 *            caller
	 */
	private synchronized static void add(String path, boolean commit) {
		if (path == null) {
			throw new IllegalArgumentException();
		}

		// Check if file exists in db
		if (exists(path)) {
			logger.debug("File already in DB: " + path);
			return;
		}
		logger.debug("Adding file to DB: " + path);
		logger.debug("Number of files in log: " + files.size());

		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(path);
			FileHolder fileholder = new FileHolder(path, fc.lastModified(),
					generateMd5(fc));
			// Group object to save object handles
			ObjectGroup.createGroup(fileholder);
			files.addElement(fileholder);
			if (commit) {
				commit();
			}
		} catch (IOException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		} finally {
			try {
				if (fc != null) {
					fc.close();
				}
			} catch (Exception e) {
				ActivityLog.addMessage(new ErrorMessage(e));
			}
		}
	}

	/**
	 * Remove the file entry from the persistent store. Does not actually delete
	 * the file on the filesystem.
	 * 
	 * @param path
	 *            the full path to the file.
	 */
	public synchronized static void delete(String path) {
		if (path == null) {
			throw new IllegalArgumentException();
		}

		for (Enumeration e = files.elements(); e.hasMoreElements();) {
			FileHolder fileholder = (FileHolder) e.nextElement();
			if (fileholder.getPath().equals(path)) {
				files.removeElement(fileholder);
				commit();
				// Add notification to activity log
				FileMessage fm = new FileMessage();
				fm.delete(path, fileholder.getModTime(), fileholder.getMd5());
				ActivityLog.addMessage(fm);
				break;
			}
		}
	}

	/**
	 * File has been renamed. Updates the path of the file entry.
	 * 
	 * @param path
	 *            the new full path.
	 * @param oldPath
	 */
	public synchronized static void renamed(String newPath, String oldPath) {
		if (newPath == null || oldPath == null) {
			throw new IllegalArgumentException();
		}

		logger.debug(newPath + " has been renamed from: " + oldPath);

		for (Enumeration e = files.elements(); e.hasMoreElements();) {
			FileHolder fileholder = (FileHolder) e.nextElement();
			if (fileholder.getPath().equals(oldPath)) {
				// Object must be ungrouped to modify it
				if (ObjectGroup.isInGroup(fileholder)) {
					fileholder = (FileHolder) ObjectGroup
							.expandGroup(fileholder);
				}
				fileholder.setPath(newPath);
				// Regroup object
				ObjectGroup.createGroup(fileholder);
				commit();
				// Add notification to activity log
				FileMessage fm = new FileMessage();
				fm.update(newPath, oldPath, fileholder.getMd5());
				ActivityLog.addMessage(fm);
				break;
			}
		}
	}

	/**
	 * File has been modified, written to or truncated. Updates the md5 and
	 * modification time of the file.
	 * 
	 * @param path
	 *            the full path of the file.
	 */
	public synchronized static void changed(String path) {
		if (path == null) {
			throw new IllegalArgumentException();
		}

		for (Enumeration enum = files.elements(); enum.hasMoreElements();) {
			FileHolder fileholder = (FileHolder) enum.nextElement();
			if (fileholder.getPath().equals(path)) {
				// Object must be ungrouped to modify it
				if (ObjectGroup.isInGroup(fileholder)) {
					fileholder = (FileHolder) ObjectGroup
							.expandGroup(fileholder);
				}
				FileConnection fc = null;
				try {
					fc = (FileConnection) Connector.open(path);
					fileholder.setModTime(fc.lastModified());
					fileholder.setMd5(generateMd5(fc));
					// Regroup object
					ObjectGroup.createGroup(fileholder);
					commit();
				} catch (IOException e) {
					ActivityLog.addMessage(new ErrorMessage(e));
				} finally {
					try {
						if (fc != null) {
							fc.close();
						}
					} catch (Exception e) {
						ActivityLog.addMessage(new ErrorMessage(e));
					}
				}
				break;
			}
		}
	}

	/**
	 * Upload all files that are marked as not uploaded.
	 */
	public synchronized static void upload() {
		// Only upload if have received instruction from server to upload or
		// connected to wifi
		if (mobileSync
				|| (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED)
				|| DeviceInfo.isSimulator()) {
			// Get all files that have no been uploaded
			for (Enumeration enum = files.elements(); enum.hasMoreElements();) {
				FileHolder fileholder = (FileHolder) enum.nextElement();
				if (!fileholder.isUploaded()) {
					FileConnection fc = null;
					try {
						// Get handle to file
						String path = fileholder.getPath();
						fc = (FileConnection) Connector.open(path);
						// Upload to server
						FileMessage fm = new FileMessage();
						fm.add(path, fileholder.getModTime(),
								fileholder.getMd5());
						try {
							Response response = Server.postMultiPart(
									fm.toString(), fc, "userfile");
							Reply.Regular reply = new Reply.Regular(
									response.getContent());
							// If server successfully processed mark as uploaded
							if (!reply.error) {
								// Object must be ungrouped to modify it
								if (ObjectGroup.isInGroup(fileholder)) {
									fileholder = (FileHolder) ObjectGroup
											.expandGroup(fileholder);
								}
								fileholder.setUploaded(true);
								// Regroup object
								ObjectGroup.createGroup(fileholder);
							}
						} catch (IOException e) {
							logger.warn("Connection problem: " + e.getMessage());
						} catch (ParseException e) {
							ActivityLog.addMessage(new ErrorMessage(e));
						}
					} catch (IOException e) {
						ActivityLog.addMessage(new ErrorMessage(e));
					} finally {
						try {
							if (fc != null) {
								fc.close();
							}
						} catch (IOException e1) {
							ActivityLog.addMessage(new ErrorMessage(e1));
						}
					}
				}
			}
			// Commit outside loop as there might be many changes
			commit();
		}
	}

	/**
	 * Check if file has already been logged
	 * 
	 * @param path
	 *            the full path of the file.
	 * @return true if exists, false otherwise.
	 */
	public synchronized static boolean exists(String path) {
		for (Enumeration e = files.elements(); e.hasMoreElements();) {
			FileHolder fileholder = (FileHolder) e.nextElement();
			if (fileholder.getPath().equals(path)) {
				return true;
			}
		}
		return false;
	}

	public static void commit() {
		// Note: store does not need to be synchronized since the calling
		// methods are already synchronized
		store.setContents(files);
		store.commit();
	}

	/**
	 * Generates the MD5 of a file.
	 * 
	 * @param fc
	 *            the open file
	 * @return hex formatted MD5.
	 */
	private static String generateMd5(FileConnection fc) {
		String md5 = "";
		DigestInputStream dis = null;

		try {
			MD5Digest md = new MD5Digest();
			// Calculate the digest for the given file.
			dis = new DigestInputStream(md, fc.openInputStream());
			byte[] buffer = new byte[8192];
			while (dis.read(buffer) != -1)
				;
			byte[] digest = md.getDigest();
			// Convert to hexstring
			for (int i = 0; i < digest.length; i++) {
				md5 += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
		} catch (Exception e) {
			ActivityLog.addMessage(new ErrorMessage(e));
		} finally {
			try {
				if (dis != null) {
					dis.close();
				}
			} catch (IOException e) {
				ActivityLog.addMessage(new ErrorMessage(e));
			}
		}
		return md5;
	}
}

class FileMessage extends Message {
	private static MMTools tools = ToolsBB.getInstance();

	/**
	 * Message format:
	 * <ul>
	 * <li>Device time
	 * <li>Action: ADD, UPD or DEL
	 * <li>File path
	 * <li>File modification time or old path (if renamed)
	 * <li>File MD5 hex checksum
	 * </ul>
	 */
	public FileMessage() {
		super(Message.FILE);
		add(tools.getDate());
	}

	public void add(String path, long modTime, String md5) {
		add("ADD");
		fill(path, String.valueOf(modTime), md5);
	}

	public void update(String path, String oldPath, String md5) {
		add("UPD");
		fill(path, oldPath, md5);
	}

	public void delete(String path, long modTime, String md5) {
		add("DEL");
		fill(path, String.valueOf(modTime), md5);
	}

	private void fill(String path, String pathORmodtime, String md5) {
		add(path);
		add(pathORmodtime);
		add(md5);
	}
}