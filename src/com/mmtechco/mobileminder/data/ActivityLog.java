package com.mmtechco.mobileminder.data;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class ActivityLog {
	private static final String TAG = ToolsBB
			.getSimpleClassName(ActivityLog.class);
	public static final long ID = StringUtilities
			.stringHashToLong(ActivityLog.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector log;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			log = new ContentProtectedVector();
			store.setContents(log);
		}
		log = (ContentProtectedVector) store.getContents();
	}

	public static synchronized void addMessage(Object message) {
		log.addElement(message.toString());
		commit();
		sendMessages();
	}

	public static synchronized boolean removeMessage() {
		try {
			log.removeElementAt(0);
			commit();
			return true;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public static synchronized String getMessage() {
		String msg = "";
		try {
			msg = (String) log.firstElement();
		} catch (NoSuchElementException e) {
			Logger.log(TAG, e.getMessage());
		}
		return msg;
	}

	public static synchronized boolean isEmpty() {
		if (log.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static synchronized int length() {
		return log.size();
	}

	private static void commit() {
		store.setContents(log);
		store.commit();
	}
	
	public static void sendMessages() {
		new Thread() {
			public void run() {
				for (Enumeration enum = log.elements(); enum.hasMoreElements();) {
					Response response;
					try {
						response = Server.get((String) enum.nextElement());
						Reply reply = new Reply(response.getContent());
						if (!reply.isError()) {
							log.removeElement(enum.nextElement());
						}
					} catch (Exception e) {
						Logger.log(TAG, e.getMessage());
					}
				}
				commit();
			}
		}.start();
	}
}
