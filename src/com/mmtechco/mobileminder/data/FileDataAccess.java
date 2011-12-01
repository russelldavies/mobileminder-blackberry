package com.mmtechco.mobileminder.data;

import java.io.IOException;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.DatabaseException;

import com.mmtechco.mobileminder.prototypes.FileDataWriter;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * This class creates a table in the local database which stores all the
 * information about the file system which will be updated on the server.
 */
public class FileDataAccess extends DBAccess implements FileDataWriter {
	private static final String TAG = ToolsBB
			.getSimpleClassName(FileDataAccess.class);

	// Database table
	protected static final String DATABASE_TABLE = "filesystem";
	// Database fields
	public static final String KEY_NEW = "new";
	public static final String KEY_FOUND = "found";
	public static final String KEY_SENT = "sent";
	public static final String KEY_NAME = "name";
	public static final String KEY_DIRECTORY = "directory";
	public static final String KEY_PATH = "path";
	public static final String KEY_TIME = "time";
	public static final String KEY_SIZE = "size";
	public static final String KEY_MD5 = "md5";

	public FileDataAccess() throws IOException {
		super();
	}

	protected String getDBTable() {
		return DATABASE_TABLE;
	}

	public void add(FileContainer fc) {
		logger.log(TAG, "Adding file to DB. File info: " + fc);
		logger.log(TAG, "DB Size: " + length());

		String sqlAddValues = "INSERT INTO " + getDBTable() + "(" + KEY_NEW
				+ "," + KEY_FOUND + "," + KEY_SENT + "," + KEY_NAME + ","
				+ KEY_DIRECTORY + "," + KEY_PATH + "," + KEY_TIME + ","
				+ KEY_SIZE + "," + KEY_MD5 + ") VALUES (1,1,0," + fc.getName()
				+ "," + fc.getDir() + "," + fc.getPath() + ","
				+ fc.getModTime() + "," + fc.getSize() + "," + fc.getMd5()
				+ ")";
		sqlExecute(sqlAddValues);
	}

	/**
	 * Sets the 'new' and 'found' column values of each row to false
	 */
	public synchronized void clean() {
		String value = "0";
		String sql = "UPDATE " + getDBTable() + " SET " + KEY_NEW + "=" + value
				+ "," + KEY_FOUND + "=" + value;
		sqlExecute(sql);
	}

	/**
	 * Retrieves a table from the db with all the index, found, name, path and
	 * size values for each row
	 * 
	 * @return cursor to the new table
	 */
	public Cursor fileCheckTable() {
		String sqlStatement = "SELECT " + KEY_INDEX + "," + KEY_FOUND + ","
				+ KEY_NAME + "," + KEY_PATH + "," + KEY_SIZE;
		return sqlQuery(sqlStatement);
	}

	/**
	 * Sets the found value to true, using the file name to index the specified
	 * row in the table
	 * 
	 * @param name
	 *            - name of the file
	 */
	public void setFound(String name) {
		String value = "1";
		String sql = "UPDATE " + getDBTable() + " SET " + KEY_FOUND + "="
				+ value + " WHERE " + KEY_NAME + "=" + name;
		sqlExecute(sql);
	}

	/**
	 * Sets the sent value to true, using the file name to index the specified
	 * row in the table
	 * 
	 * @param path
	 *            - name and path of the file
	 */
	public void setSent(String path) {
		String value = "1";
		String sql = "UPDATE " + getDBTable() + " SET " + KEY_SENT + "="
				+ value + " WHERE " + KEY_PATH + "=" + path;
		sqlExecute(sql);
		logger.log(TAG, path + " has been set as sent");
	}

	/**
	 * Sets the md5 value for the last (a.k.a. the most recently added) row set
	 * in the db This reference is used if there is no reference held for the
	 * file
	 * 
	 * @param md5
	 *            - the new md5 value for the file
	 */
	public void setMd5(String md5) {
		// get the index of the last row in table
		String sqlStatement = "SELECT " + KEY_INDEX + " FROM " + getDBTable()
				+ " ORDER BY " + KEY_INDEX + "DESC LIMIT 1";
		Cursor index = sqlQuery(sqlStatement);
		try {
			index.first();
			String key = index.getRow().getString(
					index.getColumnIndex(KEY_INDEX));
			logger.log(TAG, "Index for set md5: " + key);
			logger.log(TAG, "MD5 for set md5: " + md5);
			// update the md5 at that index
			sqlStatement = "UPDATE " + getDBTable() + " SET " + KEY_INDEX + "="
					+ key;
			sqlExecute(sqlStatement);
			index.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (DataTypeException e) {
			e.printStackTrace();
		}
	}

	public String[] findDuplicate(String pathToSearch) {
		String duplicateQuery = "SELECT * FROM " + DATABASE_TABLE
				+ " a INNER JOIN " + DATABASE_TABLE + " b ON a." + KEY_MD5
				+ " = b." + KEY_MD5 + " WHERE b.path=" + pathToSearch;

		// select path from storeDB where md5 = select md5 from storeDB where
		// path = path
		String[] pathsFound;
		Cursor duplicatesCursor = sqlQuery(duplicateQuery);
		try {
			duplicatesCursor.last();
			int cursorCount = duplicatesCursor.getPosition();
			if (cursorCount < 1) {
				pathsFound = new String[cursorCount];
				duplicatesCursor.first();
				// fileTable loop
				duplicatesCursor.isEmpty();
				while (duplicatesCursor.isEmpty() == false && 0 != cursorCount) {
					pathsFound[duplicatesCursor.getPosition()] = duplicatesCursor
							.getRow().getString(
									duplicatesCursor.getColumnIndex("path"));
					duplicatesCursor.next();
					cursorCount--;
				}
			} else {
				pathsFound = new String[1];
				pathsFound[0] = pathToSearch;
			}
			duplicatesCursor.close();
			return pathsFound;
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (DataTypeException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves a table from the db with all the files specified as "new"
	 * 
	 * @return cursor to the new table
	 */
	public Cursor getNewFileTable() {
		String sqlStatement = "SELECT * FROM " + getDBTable() + " WHERE "
				+ KEY_NEW + "=1";
		return sqlQuery(sqlStatement);
	}

	/**
	 * Retrieves a table from the db with all the files specified as "found"
	 * 
	 * @return cursor to the new table
	 */
	public Cursor getNotFoundFileTable() {
		String sqlStatement = "SELECT * FROM " + getDBTable() + " WHERE "
				+ KEY_FOUND + "=0";
		return sqlQuery(sqlStatement);
	}

	public Cursor getUnSentFileTable() {
		String sqlStatement = "SELECT * FROM " + getDBTable() + " WHERE "
				+ KEY_SENT + "=0";
		return sqlQuery(sqlStatement);
	}

	/**
	 * Updates the md5 value for the last (a.k.a. the most recently added) row
	 * set in the db
	 * 
	 * @param index
	 *            file index
	 * @param md5
	 *            file md5
	 * @param time
	 *            last modified time
	 * @param size
	 *            file size
	 */
	public void updateMd5(int index, String md5, long time, long size) {
		// update the md5 and time at that specified md5 value
		String sql = "UPDATE " + getDBTable() + " SET " + KEY_MD5 + "=" + md5
				+ ", " + KEY_TIME + "=" + time + ", " + KEY_SIZE + "=" + size
				+ " WHERE " + KEY_INDEX + "=" + index;
		sqlExecute(sql);
	}
}