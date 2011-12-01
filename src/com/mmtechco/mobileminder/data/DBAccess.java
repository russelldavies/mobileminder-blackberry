package com.mmtechco.mobileminder.data;

import java.io.IOException;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.database.DatabaseIOException;
import net.rim.device.api.database.DatabasePathException;
import net.rim.device.api.database.DatabaseSecurityOptions;
import net.rim.device.api.database.Statement;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.system.ControlledAccessException;

import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

public abstract class DBAccess {
	private static final String TAG = ToolsBB.getSimpleClassName(DBAccess.class);

	protected Logger logger = Logger.getInstance();
	protected ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	protected static Database db; // TODO: check can be non-static

	// Database definitions
	private static final String DATABASE_NAME = "mobileminder.db";
	private static final String[] DATABASE_TABLES = {
			FileDataAccess.DATABASE_TABLE, LocalDataAccess.DATABASE_TABLE };
	protected static final String KEY_INDEX = "_id";// By Android convention,
													// the primary key MUST be
													// named '_id'
	// Database table create statements
	private static final String CREATE_TABLE_LOCAL = "create table "
			+ LocalDataAccess.DATABASE_TABLE + "(" + KEY_INDEX
			+ " integer primary key autoincrement, " + LocalDataAccess.KEY_TIME
			+ " integer not null, " + LocalDataAccess.KEY_VALUE + " text"
			+ ");";
	private static final String CREATE_TABLE_FILE = "create table "
			+ FileDataAccess.DATABASE_TABLE + "(" + KEY_INDEX
			+ " integer primary key autoincrement, " + FileDataAccess.KEY_NEW
			+ " integer, " + FileDataAccess.KEY_FOUND + " integer, "
			+ FileDataAccess.KEY_SENT + " integer, " + FileDataAccess.KEY_NAME
			+ " text, " + FileDataAccess.KEY_DIRECTORY + " text, "
			+ FileDataAccess.KEY_PATH + " text, " + FileDataAccess.KEY_TIME
			+ " bigint, " + FileDataAccess.KEY_SIZE + " bigint, "
			+ FileDataAccess.KEY_MD5 + " text" + ");";
	private static final String[] DATABASE_CREATES = { CREATE_TABLE_LOCAL,
			CREATE_TABLE_FILE };

	protected static String dbLocSD = "file:///SDCard/Databases/MobileMinder/";
	protected static String dbLocStore = "file:///store/home/user/Databases/MobileMinder/";
	protected static String dbLocation = dbLocSD + DATABASE_NAME;

	public DBAccess() throws IOException {
		// If sdcard is not available and device has eMMC builtin memory
		// (filesystem mount point "/system" must be mounted for this to be
		// true) then put db on /store. Otherwise app can't run.
		if (!ToolsBB.fsMounted(FILESYSTEM.SDCARD) && ToolsBB.fsMounted(FILESYSTEM.SYSTEM)) {
				// sdcard not mounted but device has eMMC
				dbLocation = dbLocStore + DATABASE_NAME;
		} else if (!ToolsBB.fsMounted(FILESYSTEM.SDCARD) && !ToolsBB.fsMounted(FILESYSTEM.SYSTEM)) {
			// No writable storage available.
			throw new IOException();
		}
	}

	/**
	 * Opens the database. If cannot be opened attempts to create a new instance
	 * of the database. If it cannot be created throw an exception to signal
	 * failure. Note that once the database is opened it is cached so multiple
	 * calls to open an already open database have no impact.
	 * 
	 * @return this (self-reference, allowing this to be chained in an
	 *         initialization call).
	 */
	public DBAccess open() throws DatabaseIOException {
		// Only one instance of storeDB desired
		if (db == null) {
			try {
				URI dbURI = URI.create(dbLocation);
				// Open db otherwise create it
				if (DatabaseFactory.exists(dbURI)) {
					db = DatabaseFactory.open(dbURI);
				} else {
					db = createDB(dbLocation);
				}
			} catch (DatabasePathException e) {
				logger.log(TAG, "Invalid DB path.");
			} catch (ControlledAccessException e) {
				logger.log(TAG, "Cannot open DB: no read permission.");
			} catch (DatabaseException e) {
				logger.log(TAG, "Error with DB.");
			} catch (Exception e) {
				logger.log(TAG, "DB Exception");
			}
		}
		return this;
	}

	/**
	 * Creates a database in specified location and populates it with necessary
	 * schema.
	 * 
	 * @param dbLocation
	 *            - string in proper URI format specifying where the database
	 *            should created.
	 * @return
	 * @throws IllegalArgumentException
	 * @throws MalformedURIException
	 * @throws DatabaseException
	 */
	private Database createDB(String dbLocation)
			throws IllegalArgumentException, MalformedURIException,
			DatabaseException {
		URI dbURI = URI.create(dbLocation);
		// Setup encryption and create encrypted db
		// DatabaseSecurityOptions dbso = new DatabaseSecurityOptions(true);
		// TODO: enable security
		// db = DatabaseFactory.create(dbURI, dbso);
		db = DatabaseFactory.create(dbURI);
		// Open db and create schema
		// Create statements must be executed individually
		for (int i = 0; i < DATABASE_CREATES.length; i++) {
			Statement st = db.createStatement(DATABASE_CREATES[i]);
			st.prepare();
			st.execute();
			st.close();
		}
		// storeDB.close();
		return db;
	}

	/**
	 * Close access to the DB. Should be called when device is shutting down.
	 */
	public static void close() {
		try {
			db.close();
		} catch (DatabaseIOException e) {
			Logger.getInstance().log(TAG,
					"Could not close DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Used to reference the values of subclasses
	protected abstract String getDBTable();

	/*
	 * Generic CRUD methods
	 */
	/**
	 * Retrieves contents of entire local database.
	 * 
	 * @return a Cursor which contains all the information of the table used to
	 *         store phone actions.
	 * @throws DatabaseException
	 */
	public Cursor getAll() throws DatabaseException {
		// SQL statement:
		// SELECT * FROM DATABASE_TABLE
		Statement st = db.createStatement("SELECT * FROM " + getDBTable());
		st.prepare();
		return st.getCursor();
	}

	public synchronized void delete(int index) {
		// SQL statement:
		// DELETE FROM DATABASE_TABLE WHERE KEY_INDEX=index
		String sqlRemoveVal = "DELETE FROM " + getDBTable() + " WHERE "
				+ KEY_INDEX + "=" + String.valueOf(index);
		sqlExecute(sqlRemoveVal);
	}

	public void removeFirst() {
		try {
			Cursor result = getAll();
			result.first();
			// Get '_id' column (zero index) and remove
			delete(result.getRow().getInteger(0));
			result.close();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not read db.");
			e.printStackTrace();
		} catch (DataTypeException e) {
			logger.log(TAG, "Could not remove db row.");
			e.printStackTrace();
		}
	}

	public String getValue(int index) {
		String value = "";
		try {
			Cursor result = getAll();
			result.position(index);
			// The 'value' column index is 2
			value = result.getRow().getString(2);
			result.close();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not read db.");
			e.printStackTrace();
		} catch (DataTypeException e) {
			logger.log(TAG, "Could not retreive db value.");
			e.printStackTrace();
		}
		return value;
	}

	public int length() {
		int size = 0;
		try {
			Cursor result = getAll();
			result.last();
			size = result.getPosition();
			result.close();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not get db length.");
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * Takes an SQL update statement and executes it on the db.
	 * 
	 * @param sqlStatement
	 *            - a legal SQL statement which updates the db. The query must
	 *            not contain statements that return a result set. INSERT,
	 *            UPDATE, DELETE, and similar SQL statements are allowed.
	 */
	protected synchronized void sqlExecute(String sqlStatement) {
		// Primitive checking to make sure illegal statements
		// are not executed.
		if (sqlStatement.indexOf("SELECT") != -1) {
			return;
		}
		try {
			Statement st = db.createStatement(sqlStatement);
			st.prepare();
			st.execute();
			st.close();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not execute SQL statement: " + sqlStatement);
			logger.log(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Takes an SQL query statement (SELECT) and executes it on the db.
	 * 
	 * @param sqlStatement
	 *            - a legal SQL statement which returns a result set. The SELECT
	 *            statement is allowed.
	 */
	protected Cursor sqlQuery(String sqlStatement) {
		// Primitive checking to make sure illegal statements
		// are not executed.
		if (sqlStatement.indexOf("SELECT") == -1) {
			return null;
		}
		try {
			Statement st = db.createStatement(sqlStatement);
			st.prepare();
			return st.getCursor();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not execute SQL statement: " + sqlStatement);
			e.printStackTrace();
		}
		return null;
	}
}