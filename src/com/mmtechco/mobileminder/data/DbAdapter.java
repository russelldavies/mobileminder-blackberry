package com.mmtechco.mobileminder.data;

import net.rim.device.api.database.Cursor;
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
import net.rim.device.api.system.RuntimeStore;

import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;
import com.mmtechco.mobileminder.util.Constants;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.StorageException;
import com.mmtechco.mobileminder.util.ToolsBB;

public abstract class DbAdapter {
	protected static final String TAG = ToolsBB
			.getSimpleClassName(DbAdapter.class);

	// com.mmtechco.mobileminder.db
	public static final long DB_ID = 0xae7374baf7ae6095L;

	protected static Logger logger = Logger.getInstance();
	protected static ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	// Database definitions
	protected static final String DATABASE_NAME = "mobileminder.db";
	protected static final String[] DATABASE_TABLES = {
			FileDb.DATABASE_TABLE, LogDb.DATABASE_TABLE };
	private static final String[] DATABASE_CREATES = {
			LogDb.CREATE_STATEMENT, FileDb.CREATE_STATEMENT };
	// Generic database field
	protected static final String KEY_INDEX = "_id";

	protected static String dbLocSD = "file:///SDCard/Databases/MobileMinder/";
	protected static String dbLocStore = "file:///store/home/user/Databases/MobileMinder/";
	public static String dbLocation = dbLocSD + DATABASE_NAME;

	public DbAdapter() throws StorageException {
		// If sdcard is not available and device has eMMC builtin memory
		// (filesystem mount point "/system" must be mounted for this to be
		// true) then put db on /store. Otherwise app can't run.
		if (!ToolsBB.fsMounted(FILESYSTEM.SDCARD)
				&& ToolsBB.fsMounted(FILESYSTEM.SYSTEM)) {
			// sdcard not mounted but device has eMMC
			dbLocation = dbLocStore + DATABASE_NAME;
		} else if (!ToolsBB.fsMounted(FILESYSTEM.SDCARD)
				&& !ToolsBB.fsMounted(FILESYSTEM.SYSTEM)) {
			// No writable storage available.
			throw new StorageException("No writeable storage available.");
		}
	}

	/**
	 * Opens the database. If cannot be opened attempts to create a new instance
	 * of the database. If it cannot be created throw an exception to signal
	 * failure. Note that once the database has been opened it is cached so
	 * further open calls are idempotent.
	 * 
	 * @return this (self-reference, allowing this to be chained in an
	 *         initialization call).
	 * @throws DatabaseException
	 */
	public DbAdapter open() throws DatabaseException {
		getDb();
		return this;
	}

	protected Database getDb() throws DatabaseException {
		Database db;
		RuntimeStore runtimeStore = RuntimeStore.getRuntimeStore();

		synchronized (runtimeStore) {
			db = (Database) runtimeStore.get(DB_ID);
			if (db == null) {
				URI dbURI;
				try {
					dbURI = URI.create(dbLocation);
					// Open db otherwise create it
					if (DatabaseFactory.exists(dbURI)) {
						db = DatabaseFactory.open(dbURI);
					} else {
						db = createDB(dbLocation);
					}
					runtimeStore.put(DB_ID, db);
				} catch (IllegalArgumentException e) {
					logger.log(TAG, "URI is null");
					logger.log(TAG, e.getMessage());
				} catch (MalformedURIException e) {
					logger.log(TAG, "URI is invalid");
					logger.log(TAG, e.getMessage());
				} catch (ControlledAccessException e) {
					logger.log(TAG, "Cannot open DB: no read permission.");
					logger.log(TAG, e.getMessage());
				} catch (DatabasePathException e) {
					logger.log(TAG, "Invalid DB path.");
					logger.log(TAG, e.getMessage());
				} catch (DatabaseIOException e) {
					logger.log(TAG, e.getMessage());
				}
			}
		}
		return db;
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
		Database db;
		if (Constants.DEBUG) {
			// Create regular plaintext db
			db = DatabaseFactory.create(dbURI);
		} else {
			// Setup encryption and create encrypted db
			DatabaseSecurityOptions dbso = new DatabaseSecurityOptions(true);
			db = DatabaseFactory.create(dbURI, dbso);
		}
		// Open db and create schema. SQL statements must be executed
		// individually
		for (int i = 0; i < DATABASE_CREATES.length; i++) {
			Statement st = null;
			try {
				st = db.createStatement(DATABASE_CREATES[i]);
				st.prepare();
				st.execute();
			} finally {
				if (st != null) {
					st.close();
				}
			}
		}
		return db;
	}

	/**
	 * Close access to the DB. Should be called when device is shutting down.
	 * 
	 * @throws DatabaseException
	 */
	public void close() throws DatabaseException {
		try {
			Database db = getDb();
			db.close();
		} catch (DatabaseIOException e) {
			Logger.getInstance().log(TAG,
					"Could not close DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Used to reference the values of subclasses
	protected abstract String getDbTable();

	// Generic operations
	public synchronized void delete(int index) {
		String sqlRemoveVal = "DELETE FROM " + getDbTable() + " WHERE "
				+ KEY_INDEX + "=" + String.valueOf(index);
		sqlExecute(sqlRemoveVal);
	}

	public synchronized void removeFirst() {
		String sqlStatement = "DELETE FROM " + getDbTable()
				+ " WHERE rowid=(SELECT rowid FROM " + getDbTable()
				+ " LIMIT 1)";
		sqlExecute(sqlStatement);
	}

	public synchronized boolean isEmpty() {
		boolean empty = true;
		String sqlStatement = "SELECT * FROM " + getDbTable() + " LIMIT 1";

		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			empty = !c.next();
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
		return empty;
	}

	public synchronized int length() {
		int length = 0;
		String sqlStatement = "SELECT " + KEY_INDEX + " FROM " + getDbTable();

		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			Cursor c = st.getCursor();
			c.last();
			length = c.getPosition();
			c.close();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not get db length.");
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
		return length;
	}

	/**
	 * Takes an SQL update statement and executes it on the db.
	 * 
	 * @param sqlStatement
	 *            - a legal SQL statement which updates the db. The query must
	 *            <b>not</b> contain statements that return a result set.
	 *            <p>
	 *            INSERT, UPDATE, DELETE, and similar SQL statements are
	 *            allowed.
	 *            </p>
	 */
	protected synchronized boolean sqlExecute(String sqlStatement) {
		boolean executed = true;
		Statement st = null;
		try {
			Database db = getDb();
			st = db.createStatement(sqlStatement);
			st.prepare();
			st.execute();
		} catch (DatabaseException e) {
			logger.log(TAG, "Could not execute SQL statement: " + sqlStatement);
			logger.log(TAG, e.getMessage());
			e.printStackTrace();
			executed = false;
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (DatabaseException e) {
					logger.log(TAG, e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return executed;
	}
}