package com.mmtechco.mobileminder.data;

import java.util.Vector;

import com.mmtechco.mobileminder.util.Constants;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public abstract class DbLegacy {
	private static final String TAG = ToolsBB.getSimpleClassName(DbLegacy.class);
	
	protected static Logger logger = Logger.getInstance();
	
	protected static PersistentObject store = PersistentStore
			.getPersistentObject(Constants.GUID);
	protected static Vector localData; // list of Actions
	
	
	public DbLegacy open() {
		synchronized (store) {
			if (null == store.getContents()) {
				store.setContents(new Vector());
				store.commit();
			}
		}
		localData = new Vector();
		localData = (Vector) store.getContents();

		return this;
	}
	
	/**
	 * Retrieves the size of the object
	 * 
	 * @return integer size of the object
	 */
	public int length() {
		synchronized (store) {
			localData = (Vector) store.getContents();
			return localData.size();
		}
	}
}
