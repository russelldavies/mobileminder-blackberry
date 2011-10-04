package com.mmtechco.mobileminder.data;

import java.io.IOException;

import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.Message;

/**
 * This class creates a table in the local database which stores all the
 * information which need to be sent to the web server.
 * 
 * Uses SQLite which is only present in SDK 5.0 and up.
 */
public class LocalDataAccess extends DBAccess implements LocalDataWriter {
	// Database table
	protected static final String DATABASE_TABLE = "localdata";
	// Database fields
	protected static final String KEY_TIME = "time";
	protected static final String KEY_VALUE = "value";

	public LocalDataAccess() throws IOException {
		super();
	}
	
	protected String getDBTable() {
		return DATABASE_TABLE;
	}

	public synchronized void addMessage(Message message) {
		// SQL statement:
		// INSERT INTO DATABASE_TABLE(KEY_TIME,KEY_VALUE) VALUES
		// ('[date&time]','message contents')
		String sqlAddMessage = "INSERT INTO " + DATABASE_TABLE + "(" + KEY_TIME
				+ "," + KEY_VALUE + ") VALUES ('" + tools.getDate() + "','"
				+ message.getREST() + "')";
		update(sqlAddMessage);
	}
}