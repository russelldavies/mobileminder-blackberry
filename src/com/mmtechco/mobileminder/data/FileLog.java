package com.mmtechco.mobileminder.data;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.crypto.DigestInputStream;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

public class FileLog {
	private static final String TAG = ToolsBB.getSimpleClassName(FileLog.class);
	public static final long ID = StringUtilities
			.stringHashToLong(FileLog.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector files;

	protected static Logger logger = Logger.getInstance();

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
	 * 
	 * @param path
	 *            Fully qualified path to the file
	 */
	public synchronized static void add(String path) {
		add(path, true);
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
	public synchronized static void add(String path, boolean commit) {
		// Check if file exists in db
		if (exists(path)) {
			logger.log(TAG, "File already in DB: " + path);
			return;
		}
		logger.log(TAG, "Adding file to DB: " + path);
		logger.log(TAG, "Number of files in log: " + files.size());

		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(path);
			FileHolder fileholder = new FileHolder(path, fc.lastModified(),
					generateMd5(fc));
			files.addElement(fileholder);
			if (commit) {
				commit();
			}
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

	/**
	 * Remove the file entry from the persistent store. Does not actually delete
	 * the file on the filesystem.
	 * 
	 * @param path
	 *            the full path to the file.
	 */
	public synchronized static void delete(String path) {
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
		logger.log(TAG, newPath + " has been renamed from: " + oldPath);

		for (Enumeration e = files.elements(); e.hasMoreElements();) {
			FileHolder fileholder = (FileHolder) e.nextElement();
			if (fileholder.getPath().equals(oldPath)) {
				fileholder.setPath(newPath);
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
		for (Enumeration enum = files.elements(); enum.hasMoreElements();) {
			FileHolder fileholder = (FileHolder) enum.nextElement();
			if (fileholder.getPath().equals(path)) {
				FileConnection fc = null;
				try {
					fc = (FileConnection) Connector.open(path);
					fileholder.setModTime(fc.lastModified());
					fileholder.setMd5(generateMd5(fc));
					commit();
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
				|| (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED)) {
			// Get all files that have no been uploaded
			for (Enumeration e = files.elements(); e.hasMoreElements();) {
				FileHolder fileholder = (FileHolder) e.nextElement();
				if (fileholder.isUploaded() == false) {
					FileConnection fc = null;
					try {
						// Get handle to file
						String path = fileholder.getPath();
						fc = (FileConnection) Connector.open(path);
						// Upload to server
						FileMessage fm = new FileMessage();
						fm.add(path, fileholder.getModTime(),
								fileholder.getMd5());
						String reply = new Server()
								.contactServer(fm.getREST(), fc).getREST()
								.toLowerCase();
						// If server successfully processed mark as uploaded
						if (reply.indexOf("ok") != -1) {
							fileholder.setUploaded(true);
						}
					} catch (IOException e1) {
						logger.log(TAG, e1.getMessage());
					} finally {
						try {
							if (fc != null) {
								fc.close();
							}
						} catch (IOException e1) {
							logger.log(TAG, e1.getMessage());
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
			logger.log(TAG, e.toString());
		} finally {
			try {
				if (dis != null) {
					dis.close();
				}
			} catch (IOException e) {
				logger.log(TAG, e.toString());
			}
		}
		return md5;
	}
}

class FileMessage implements Message {
	private final int type = 22;
	private String action;
	private String path;
	private String modtimeORoldpath;
	private String md5;
	private MMTools tools = ToolsBB.getInstance();

	public void add(String path, long modTime, String md5) {
		action = "ADD";
		this.path = path;
		this.modtimeORoldpath = tools.getDate(modTime);
		this.md5 = md5;
	}

	public void update(String path, String oldPath, String md5) {
		action = "UPD";
		this.path = path;
		this.modtimeORoldpath = oldPath;
		this.md5 = md5;
	}

	public void delete(String path, long modTime, String md5) {
		action = "DEL";
		add(path, modTime, md5);
	}

	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator
				+ type + Tools.ServerQueryStringSeparator + tools.getDate()
				+ Tools.ServerQueryStringSeparator + action
				+ Tools.ServerQueryStringSeparator + path
				+ Tools.ServerQueryStringSeparator + modtimeORoldpath
				+ Tools.ServerQueryStringSeparator + md5;
	}

	public String getTime() {
		return modtimeORoldpath;
	}

	public int getType() {
		return type;
	}
}