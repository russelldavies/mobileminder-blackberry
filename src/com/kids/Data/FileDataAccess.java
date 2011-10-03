package com.kids.Data;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Database;

import com.kids.Logger;
import com.kids.prototypes.Debug;
/*
interface LocalDataWriter 
{
	void addValue(String inputValue);
	void addMessage(Message inputMessage);
}
*/
/**
 * This class creates a table in the local database which stores all the information about the file system which will be updated on the server.
 */
public class FileDataAccess implements FileDataWriter
{
	//private 			 Context thisContext;
	private Debug logger = Logger.getInstance();
	private static final String  DATABASE_NAME    = "CVKfilemanager";
    private static final String  DATABASE_TABLE   = "FileSystem";
    private Cursor result;
   // private static final int     DATABASE_VERSION = 2;
    //The primary key MUST be named '_id' Android convention
    private static final String  KEY_INDEX		  = "_id";//MAY OVER FLOW!!
    private static final String  KEY_NEW		  = "new";
    private static final String  KEY_FOUND		  = "found";
    private static final String  KEY_SENT		  = "sent";
    private static final String  KEY_NAME		  = "name";
    private static final String  KEY_DIRECTORY	  = "directory";
    private static final String  KEY_PATH		  = "path";
    private static final String  KEY_TIME		  = "time";
    private static final String  KEY_SIZE		  = "size";
    private static final String  KEY_MD5		  = "md5";
    private static final String  DATABASE_CREATE  = "create table `"+DATABASE_TABLE+ "`("
															   +"`"+KEY_INDEX  +"` integer NOT NULL primary key autoincrement,"
															   +"`"+KEY_NEW   +"` INTEGER,"
															   +"`"+KEY_FOUND   +"` INTEGER,"
															   +"`"+KEY_SENT   +"` INTEGER,"
															   +"`"+KEY_NAME  +"` TEXT,"
															   +"`"+KEY_DIRECTORY  +"` TEXT,"
															   +"`"+KEY_PATH  +"` TEXT,"
															   +"`"+KEY_TIME  +"` BIGINT,"
															   +"`"+KEY_SIZE  +"` BIGINT,"
															   +"`"+KEY_MD5  +"` TEXT);";
    private Database storeDB;
    private final String duplicateQuery = "SELECT "+KEY_PATH+" FROM "+DATABASE_TABLE+" a INNER JOIN "+DATABASE_TABLE+" b ON a."+KEY_MD5+" = b."+KEY_MD5+" WHERE b."+KEY_PATH+"=?";
    
/**
 * This is the constructor of FileDataAccess. It creates the environment for the table in the local database used to store files in the device file system.
 * @param _context Interface to global environment that the current application is running in.
 */
	public FileDataAccess()
	{storeDB  = new DatabaseHelper(this.getWritableDatabase());}
	
	public void openDB()
	{
		String path = _context+DATABASE_NAME;
		Database.openDatabase(path, null, Database.OPEN_READWRITE);
	}
	
	public boolean isOpen()
	{
		return storeDB.isOpen();
	}
	
	public void closeDB()
	{
		storeDB.close();
	}
	
/**
 * This method adds the files into the table used to store the file system.
 * @param _file The file information object
 */
	//@Override
	public void add(FileInfo _file) 
	{// TODO Auto-generated method stub
		addValues(_file.getName(), _file.getDirectory(), _file.getPath(), _file.getLastModifiedTime(), _file.getSize(), _file.getMd5());
	}
	
/**
 * This method adds the information to the table used to store the file system.
 * @param _name file name
 * @param _path path to the file
 * @param _time last modified time of the file
 * @param _size size in bytes of the file
 * @param _md5 md5 of the file-stream
 */
	private void addValues(String _name, String _directory, String _path, long _time, long _size, String _md5)
	{
		logger.log("Adding File to DB: "+_name+","+_directory+","+_path+","+_time+","+_size+","+_md5);
		logger.log("DB Size: "+length());
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NEW,  1);//1
        initialValues.put(KEY_FOUND,  1);//2
        initialValues.put(KEY_SENT,  0);//3
        initialValues.put(KEY_NAME, _name);//4
        initialValues.put(KEY_DIRECTORY, _directory);//5
        initialValues.put(KEY_PATH, _path);//6
        initialValues.put(KEY_TIME, _time);//7
        initialValues.put(KEY_SIZE, _size);//8
        initialValues.put(KEY_MD5, _md5);//9
        storeDB.insert(DATABASE_TABLE, null, initialValues);
	}
	
/**
 * This method retrieves first row's value in the table used to store file information.
 * @return a string contains all the information from row one in the table used to store file information.
 */
	public String getFirst()
	{return getValue(0);}
	
/**
 * This method retrieves the specified row's value in the table used to store file information.
 * @param _index of the specified row in the table used to store file information.
 * @return a string contains all the values from the specified index of the database.
 */
	public String getValue(int _index)
	{
		String value;
		result = getStoreDBoutput();
		result.moveToPosition(_index);
		value = result.getString(2);
		result.close();
		result = null;
		return value;
	}
	
/**
 * This method finds out if the db has no rows 
 * 
 * @return true if no row
 */
	public boolean isEmpty()
	{
		//select (select * from DATABASE_TABLE limit 1) is not null;
		Cursor c = storeDB.query(DATABASE_TABLE, null, null, null, null, null, null, "1");
		int count = c.getCount();
		c.close();
		c = null;
		
		if(0 == count)
		return true;
		else
		return false;
	}
	
/**
 * This method resets all the new and found values from each row to 0
 */
	public synchronized void clean()
	{
		//UPDATE DATABASE_TABLE SET new, found = NULL;
		ContentValues values = new ContentValues(); 
		values.put(KEY_NEW, 0);
		values.put(KEY_FOUND, 0);
		storeDB.update(DATABASE_TABLE, values, null, null);
	}

/**
 * This method retrieves a table from the db with all the index, found, name, path and size values for each row
 * 
 * @return cursor to the new table
 */
	public Cursor fileCheckTable()
	{return storeDB.query(DATABASE_TABLE, new String[]{KEY_INDEX, KEY_FOUND, KEY_NAME, KEY_PATH, KEY_SIZE}, null, null, null, null, null, null);}
	
/**
 * This method set the found value to true, using the file name to index the specified row in the table
 * 
 * @param _name name of the file
 */
	public void setFound(String _name)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_FOUND, 1);
		storeDB.update(DATABASE_TABLE, values,
				   		KEY_NAME+"=?",
				   		new String[] {_name});
	}
	
	
/**
 * This method set the sent value to true, using the file name to index the specified row in the table
 * 
 * @param _name name of the file
 */
	public void setSent(String _path)
	{
		
		ContentValues values = new ContentValues();
		values.put(KEY_SENT, 1);
		storeDB.update(DATABASE_TABLE, values,
				   		KEY_PATH+"=?",
				   		new String[] {_path});		
		logger.log(_path+" has been set as sent");
	}
	
/**
 * This method set the sent value to true, using the file name to index the specified row in the table
 * 
 * @param _path path to the file
 */
	public boolean isSent(String _path)
	{
		Cursor sentValue = storeDB.query(DATABASE_TABLE, new String[] {KEY_SENT},KEY_PATH+" =?", new String[] {_path}, null, null, null, null);
		sentValue.moveToFirst();
		int value = sentValue.getInt(sentValue.getColumnIndex(KEY_SENT));
		boolean sent = value != 0;
		sentValue.close();
		return sent;
	}
	
/**
 * This method gets all the values in a row, using the file path to index the specified row in the table
 * 
 * @param _path path to the file
 */
	public Cursor getFileInfo(String _path)
	{
		Cursor fileInfoValues = storeDB.query(DATABASE_TABLE, new String[] {KEY_NAME, KEY_DIRECTORY, KEY_PATH, KEY_TIME, KEY_SIZE, KEY_SIZE, KEY_MD5 },KEY_PATH+" =?", new String[] {_path}, null, null, null, null);
		return fileInfoValues;	
	}

/**
 * This method sets the md5 value for the last (a.k.a. the most recently added) row set in the db
 * This reference is used if there is no reference held for the file
 * 
 * @param _md5 new md5 value for the file
 */
	public void setMd5(String _md5)
	{
		//get the index of the last row in table
		Cursor index = storeDB.query(DATABASE_TABLE, new String[]{KEY_INDEX}, null, null, null, null, KEY_INDEX+" DESC", "1");
		index.moveToFirst();
		int key = index.getInt(index.getColumnIndex(KEY_INDEX));
		logger.log("Index for set md5: "+key);	
		logger.log("MD5 for set md5: "+_md5);	
		//update the md5 at that index
		ContentValues values = new ContentValues(); 
		values.put(KEY_MD5, _md5);
		int done = storeDB.update(DATABASE_TABLE, values,
				   		KEY_INDEX+"=?", 
				   		new String[] {String.valueOf(key)});
		logger.log("After md5 set no of rows effected: "+done);
		_md5 = null;
		index.close();
		index = null;
	}
	
	
	public String[] findDuplicate(String _path)
	{
		//select path from storeDB where md5 = select md5 from storeDB where path = path 
		String[] paths;
		Cursor duplicates = storeDB.rawQuery(duplicateQuery, new String[]{String.valueOf(_path)});
		int cursorCount = duplicates.getCount();
		paths= new String[cursorCount];
		
		duplicates.moveToFirst();
        while (duplicates.isAfterLast() == false && 0 != cursorCount )//fileTable loop
        {
        	paths[duplicates.getPosition()] = duplicates.getString(duplicates.getPosition());
        	duplicates.moveToNext();
        	cursorCount--;
        }
		
		return paths;
	}
	
	
/**
 * This method is used to get a cursor to a table with all the rows in the db.
 * It calls a private method of the class to retrieve the table.
 * 
 * @return Cursor to table
 */
	public Cursor getPrivateStoreDBoutput()
	{return getStoreDBoutput();}
	
/**
 * This method retrieves a table from the db with all the files specified as "new"
 * 
 * @return cursor to the new table
 */
	public Cursor getNewFileTable()
	{return storeDB.query(DATABASE_TABLE, null,KEY_NEW+" =?", new String[] {"1"}, null, null, null, null);}
	
/**
 * This method retrieves a table from the db with all the files specified as "found"
 * 
 * @return cursor to the new table
 */
	public Cursor getNotFoundFileTable()
	{return storeDB.query(DATABASE_TABLE, null,KEY_FOUND+" =?", new String[] {"0"}, null, null, null, null);}
	
/**
 * This method is used to set new values for the name, path and time columns in a row, it's referenced by the md5 value
 * 
 * @param _name file name
 * @param _path file path
 * @param _time last modified time 
 * @param _md5 md5 value for file
 */
	public void updateNamePath(String _name, String _path, long _time, String _md5) 
	{
		//update the name, path and time at that specified md5 value
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, _name);
		values.put(KEY_PATH, _path);
		values.put(KEY_TIME, _time);
		storeDB.update(DATABASE_TABLE, values,
				   		KEY_MD5+"=?",
				   		new String[] {_md5});
	}

/**
 * This method updates the md5 value for the last (a.k.a. the most recently added) row set in the db
 * 
 * @param _index file index
 * @param _md5 file md5
 * @param _time last modified time
 * @param _size file size
 */
	public void updateMd5(int _index, String _md5, long _time, long _size)
	{
		//update the md5 and time at that specified md5 value
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, _md5);
		values.put(KEY_TIME, _time);
		values.put(KEY_TIME, _size);
		storeDB.update(DATABASE_TABLE, values,
				   		KEY_INDEX+"=?",
				   		new String[] {String.valueOf(_index)});
	}
	
	/**
	 * This method removes the first row's value in the table used to store file information.
	 */
	public void removeFirst()
	{
		result = getStoreDBoutput();
		result.moveToPosition(0);//move to First
		delete(result.getInt(0));//Get ID and remove
		result.close();
		result = null;
	}
	/**
	 * This method delete a row in the db at the index given
	 * 
	 * @return rows no. of rows deleted
	 */
	public int delete(int _id) {
		
		int rows = storeDB.delete(DATABASE_TABLE, 
				   KEY_INDEX+"=?", 
				   new String[] {String.valueOf(_id)});
		return rows;
	}

	
	/**
	 * This method gets the length of a certain Cursor.
	 * @return The length of the current cursor object.
	 */
	public int length()
	{	
		result = getStoreDBoutput();
		int size = result.getCount();
		result.close();
		result = null;
		return size;
	}
	
	/**
	 * This method retrieves the information in the local database.
	 * @return a cursor object which contains all the information of the table used to store file information.
	 */
	private Cursor getStoreDBoutput()
	{return storeDB.query(DATABASE_TABLE, null, null, null, null, null, null);
	}
	
	/**
	 * This class is used to create the local database.
	 *
	 */
	private static class DatabaseHelper// extends SQLiteOpenHelper 
	{
		/**
		 * This is the default constructor that is needed when implementing the SOLiteOpenHelper.
		 * 
		 * @param _context Interface to global environment that the current application is running in.
		 */
		DatabaseHelper(/*Context _context*/) 
		{
			super(DATABASE_NAME, null, 2);//DATABASE_VERSION);
		}
		
		/**
		 * This method creates the database calls setupFirstEntry to initialise the local storage.
		 * @param _db SQLiteDatabase
		 */
		//@Override
		public void onCreate(Database _db) 
		{_db.execSQL(DATABASE_CREATE); }
		
		/**
		 * This method initialises the local storage.
		 * @param _db SQLiteDatabase
		 */
		//@Override
		public void onUpgrade(Database _db, int oldVersion, int _newVersion) 
		{
			_db.execSQL("DROP IF TABLE EXISTS "+DATABASE_TABLE);
			onCreate(_db);
		}
	}
}