package com.mmtechco.mobileminder.data;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.Statement;

import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.StorageException;

/**
 * This class creates a table in the local database which stores all the
 * information which need to be sent to the web server.
 * 
 * Uses SQLite which is only present in SDK 5.0 and up.
 */
public class LogDb extends DbAdapter {
	// Database table
	protected static final String DATABASE_TABLE = "log";
	// Database table create statement
	public static final String CREATE_STATEMENT = "create table "
			+ LogDb.DATABASE_TABLE + "(" + KEY_INDEX
			+ " integer primary key autoincrement, " + LogDb.KEY_TIME
			+ " integer not null, " + LogDb.KEY_VALUE + " text"
			+ ");";
	// Database fields
	protected static final String KEY_TIME = "time";
	protected static final String KEY_VALUE = "value";

	public LogDb() throws StorageException {
		super();
	}

	protected String getDbTable() {
		return DATABASE_TABLE;
	}

	public synchronized void addMessage(Message message) {
		String sqlAddMessage = "INSERT INTO " + DATABASE_TABLE + "(" + KEY_TIME
				+ "," + KEY_VALUE + ") VALUES ('" + tools.getDate() + "','"
				+ message.getREST() + "')";
		sqlExecute(sqlAddMessage);
	}

	public synchronized String getValue(int index) {
		String value = "";
		String sqlStatement = "SELECT " + KEY_VALUE + " FROM " + getDbTable()
				+ " WHERE " + KEY_INDEX + "=" + String.valueOf(index);

		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			c.next();
			value = c.getRow().getString(c.getColumnIndex(KEY_VALUE));
			c.close();
		} catch (DatabaseException e) {
			logger.log(TAG, e.getMessage());
		} catch (DataTypeException e) {
			logger.log(TAG, "Could not retreive db value.");
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
		return value;
	}
}