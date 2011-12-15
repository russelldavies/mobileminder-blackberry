package com.mmtechco.mobileminder.data;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.crypto.DigestInputStream;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.Statement;
import net.rim.device.api.system.WLANInfo;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.StorageException;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Stores a list of files on the device, of a certain criteria, in the local
 * database.
 */
public class FileDb extends DbAdapter {
	private static final String TAG = ToolsBB
			.getSimpleClassName(FileDb.class);

	// Database table
	protected static final String DATABASE_TABLE = "filesystem";
	// Database table create statement
	public static final String CREATE_STATEMENT = "create table "
			+ FileDb.DATABASE_TABLE + "(" + KEY_INDEX
			+ " integer primary key autoincrement, " + FileDb.KEY_PATH
			+ " text, " + FileDb.KEY_MODTIME + " integer,"
			+ FileDb.KEY_MD5 + " text, " + FileDb.KEY_UPLOADED
			+ " integer" + ");";
	// Database fields
	public static final String KEY_PATH = "path";
	public static final String KEY_MODTIME = "modtime";
	public static final String KEY_MD5 = "md5";
	public static final String KEY_UPLOADED = "uploaded";

	private LogDb actLog;
	public static boolean mobileSync = false;

	public FileDb(LogDb actLog) throws StorageException {
		super();
		this.actLog = actLog;
	}

	protected String getDbTable() {
		return DATABASE_TABLE;
	}

	public void add(String filePath) {
		// Check if file exists in db
		if (!exists(filePath)) {
			logger.log(TAG, "File already in DB: " + filePath);
			return;
		}

		logger.log(TAG, "Adding file to DB: " + filePath);
		logger.log(TAG, "DB Size: " + length());

		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(filePath);
			String md5 = generateMd5(fc);
			String uploaded = "0";

			String sqlStatement = "INSERT INTO " + getDbTable() + "("
					+ KEY_PATH + "," + KEY_MODTIME + "," + KEY_MD5 + ","
					+ KEY_UPLOADED + ") VALUES ('" + filePath + "','"
					+ fc.lastModified() + "','" + md5 + "','" + uploaded + "')";
			sqlExecute(sqlStatement);
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
	 * Remove the file entry from the db. Does not actually delete the file.
	 * 
	 * @param path
	 *            the full path to the file.
	 */
	public void delete(String path) {
		// Remove file entry from db
		String sqlStatement = "DELETE FROM " + getDbTable() + " WHERE "
				+ KEY_PATH + "='" + path + "'";
		sqlExecute(sqlStatement);
		// Add notification to activity log
		FileMessage fm = new FileMessage();
		fm.delete(path, getModTime(path), getMd5(path));
		actLog.addMessage(fm);
	}

	/**
	 * File has been renamed. Updates the path of the file entry.
	 * 
	 * @param path
	 *            the new full path.
	 * @param oldPath
	 */
	public void renamed(String path, String oldPath) {
		String sqlStatement = "UPDATE " + getDbTable() + " SET " + KEY_PATH
				+ "='" + path + "' WHERE " + KEY_PATH + "='" + oldPath + "'";
		sqlExecute(sqlStatement);
		logger.log(TAG, path + " has been renamed from: " + oldPath + " to: "
				+ path);
		// Add notification to activity log
		FileMessage fc = new FileMessage();
		fc.update(path, oldPath, getMd5(path));
		actLog.addMessage(fc);
	}

	/**
	 * File has been modified, written to or truncated. Updates the md5 and
	 * modification time of the file.
	 * 
	 * @param path
	 *            the full path of the file.
	 */
	public void changed(String path) {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(path);
			String md5 = generateMd5(fc);
			long modTime = fc.lastModified();
			setMd5(path, md5);
			setModTime(path, modTime);
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
	 * Upload all files that are marked as not uploaded.
	 */
	public void upload() {
		// Only upload if have received instruction from server to upload or
		// connected to wifi
		if (mobileSync
				|| (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED)) {
			// Get all files that have no been uploaded
			String uploaded = "0";
			String sqlStatement = "SELECT * FROM " + getDbTable() + " WHERE "
					+ KEY_UPLOADED + "=" + uploaded;

			Statement st = null;
			try {
				Database db = getDb();
				st = db.createStatement(sqlStatement);
				st.prepare();
				Cursor c = st.getCursor();

				while (c.next()) {
					// Get handle to file
					String path = c.getRow().getString(
							c.getColumnIndex(KEY_PATH));
					FileConnection fc = (FileConnection) Connector.open(path);
					// Upload to server
					FileMessage fm = new FileMessage();
					fm.add(path, fc.lastModified(), getMd5(path));
					String reply = new Server().contactServer(fm.getREST(), fc)
							.getREST().toLowerCase();
					// If server successfully processed mark as uploaded
					if (reply.indexOf("ok") != -1) {
						setUploaded(path);
					}
				}
			} catch (DatabaseException e) {
				logger.log(TAG, e.getMessage());
				e.printStackTrace();
			} catch (DataTypeException e) {
				logger.log(TAG, e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.log(TAG, e.getMessage());
				e.printStackTrace();
			} finally {
				if (st != null) {
					try {
						st.close();
					} catch (DatabaseException e) {
						logger.log(TAG, e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Check if path exists in db
	 * 
	 * @param path
	 *            the full path of the file.
	 * @return true if exists, false otherwise.
	 */
	public boolean exists(String path) {
		boolean exists = true;
		String sqlStatement = "SELECT " + KEY_PATH + " FROM " + getDbTable()
				+ " WHERE " + KEY_PATH + "='" + path + "'";
		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			exists = !c.next();
			c.close();
		} catch (DatabaseException e) {
			logger.log(TAG, e.getMessage());
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (DatabaseException e) {
					logger.log(TAG, e.getMessage());
				}
			}
		}
		return exists;
	}

	/**
	 * For the entry specified by its path, sets the uploaded flag to true (1).
	 * 
	 * @param path
	 *            the full path of the file.
	 */
	public void setUploaded(String path) {
		String uploaded = "1";
		String sqlStatement = "UPDATE " + getDbTable() + " SET " + KEY_UPLOADED
				+ "=" + uploaded + " WHERE " + KEY_PATH + "='" + path + "'";
		sqlExecute(sqlStatement);
		logger.log(TAG, path + " has been set as uploaded");
	}

	public void setMd5(String path, String md5) {
		String sqlStatement = "UPDATE " + getDbTable() + " SET " + KEY_MD5
				+ "=" + md5 + " WHERE " + KEY_PATH + "='" + path + "'";
		sqlExecute(sqlStatement);
		logger.log(TAG, path + " md5 has been updated to " + md5);
	}

	public String getMd5(String path) {
		String md5 = null;
		String sqlStatement = "SELECT " + KEY_MD5 + " FROM " + getDbTable()
				+ " WHERE " + KEY_PATH + "='" + path + "'";
		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			c.next();
			md5 = c.getRow().getString(c.getColumnIndex(KEY_MD5));
		} catch (DatabaseException e) {
			logger.log(TAG, e.getMessage());
		} catch (DataTypeException e) {
			logger.log(TAG, e.getMessage());
			e.printStackTrace();
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (DatabaseException e) {
					logger.log(TAG, e.getMessage());
				}
			}
		}
		return md5;
	}

	public void setModTime(String path, long modTime) {
		String sqlStatement = "UPDATE " + getDbTable() + " SET " + KEY_MODTIME
				+ "=" + modTime + " WHERE " + KEY_PATH + "='" + path + "'";
		sqlExecute(sqlStatement);
		logger.log(TAG, path + " modtime updated to " + tools.getDate(modTime));
	}

	public long getModTime(String path) {
		long modTime = 0;
		String sqlStatement = "SELECT " + KEY_MD5 + " FROM " + getDbTable()
				+ " WHERE " + KEY_MODTIME + "='" + path + "'";
		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			c.next();
			modTime = c.getRow().getLong(c.getColumnIndex(KEY_MODTIME));
		} catch (DatabaseException e) {
			logger.log(TAG, e.getMessage());
		} catch (DataTypeException e) {
			logger.log(TAG, e.getMessage());
			e.printStackTrace();
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (DatabaseException e) {
					logger.log(TAG, e.getMessage());
				}
			}
		}
		return modTime;
	}

	public String[] findDuplicate(String path) {
		String[] pathsFound = null;
		String sqlStatement = "SELECT * FROM " + getDbTable()
				+ " a INNER JOIN " + getDbTable() + " b ON a." + KEY_MD5
				+ " = b." + KEY_MD5 + " WHERE b.path=" + path;

		// select path from storeDB where md5 = select md5 from storeDB where
		// path = path
		// Find
		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			c.last();
			int cursorCount = c.getPosition();
			if (cursorCount < 1) {
				pathsFound = new String[cursorCount];
				c.first();
				// fileTable loop
				c.isEmpty();
				while (c.isEmpty() == false && 0 != cursorCount) {
					pathsFound[c.getPosition()] = c.getRow().getString(
							c.getColumnIndex(KEY_PATH));
					c.next();
					cursorCount--;
				}
			} else {
				pathsFound = new String[1];
				pathsFound[0] = path;
			}
			c.close();
		} catch (DatabaseException e) {
			logger.log(TAG, e.getMessage());
		} catch (DataTypeException e) {
			logger.log(TAG, e.getMessage());
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (DatabaseException e) {
					e.getMessage();
				}
			}
		}
		return pathsFound;
	}

	/**
	 * Generates the MD5 of a file.
	 * 
	 * @param fc
	 *            the open file
	 * @return hex formatted MD5.
	 */
	private String generateMd5(FileConnection fc) {
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