package com.mmtechco.mobileminder.prototypes;

import net.rim.device.api.database.DatabaseException;

import com.mmtechco.mobileminder.data.DBAccess;

/**
 * Defines the structure for locally writing data.
 */
public interface LocalDataWriter {
	/**
	 * Retrieves the specified row's REST String 
	 * value in the table used to store device actions.
	 * @param index of the specified row in the table used 
	 * to store device actions.
	 * @return a string contains all the values from the 
	 * specified index of the database.
	 */
	String getValue(int index);
	
	/**
	 * Removes the first row's value in the table 
	 * used to store phone actions.
	 */
	void removeFirst();
	
	/**
	 * Deletes a row in the DB at the given index.
	 * @param index - position where the where information is needed to be removed.
	 * @return number of rows deleted.
	 */
	void delete(int index);
	
	/**
	 * Gets the length of a certain Cursor.
	 * @return The length of the current cursor object.
	 */
	int length();
	
	//void addValue(String inputValue);

	/**
	 * Adds the phone actions messages into the table used
	 * to store phone actions.
	 * @param message - the phone actions from the monitor classes.
	 */
	void addMessage(Message message);

	DBAccess open() throws DatabaseException;
	
	void close() throws DatabaseException;
}
