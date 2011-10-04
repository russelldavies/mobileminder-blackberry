package com.kids.prototypes;

public interface LocalDataReader extends LocalDataWriter
{
	
	/**
	 * This method retrieves first row's value in the table used to store phone actions.
	 * @return a string contains all the information from row one in the table used to store phone actions.
	 */
	public String getFirst();
	
	/**
	 * This method retrieves the specified row's REST String value in the table used to store device actions.
	 * @param _index of the specified row in the table used to store device actions.
	 * @return a string contains all the values from the specified index of the database.
	 */
	public String getValue(int _index);
	
	/**
	 * This method removes the first row's value in the table used to store phone actions.
	 */
	public void removeFirst();
	
	/**
	 * This method removes the information at specified position of the table used to store phone actions.
	 * @param _index The position where the information is needed to be removed.
	 */
	public void removeValue(int _index);
	
	/**
	 * This method gets the length of a certain Cursor.
	 * @return The length of the current cursor object.
	 */
	public int length();
}
