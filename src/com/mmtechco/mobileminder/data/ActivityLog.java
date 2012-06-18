package com.mmtechco.mobileminder.data;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.util.Logger;

public class ActivityLog {
	public static final long ID = StringUtilities
			.stringHashToLong(ActivityLog.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector log;
	
	private static Logger logger = Logger.getLogger(ActivityLog.class);

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			log = new ContentProtectedVector();
			store.setContents(log);
		}
		log = (ContentProtectedVector) store.getContents();
	}

	public static synchronized void addMessage(Message message) {
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
			logger.error(e.getMessage());
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

	private static synchronized void sendMessages() {
		new Thread() {
			public void run() {
				try {
					for (Enumeration enum = log.elements(); enum
							.hasMoreElements();) {
						String msg = (String) enum.nextElement();
						// response = Server.get((String) enum.nextElement());
						Response response = Server.get(msg);
						Reply.Regular reply = new Reply.Regular(
								response.getContent());
						if (!reply.error) {
							log.removeElement(enum.nextElement());
						}
					}
					commit();
				} catch (IOException e) {
					logger.warn("Connection problem: " + e.getMessage());
				} catch (ParseException e) {
					ActivityLog.addMessage(new ErrorMessage(e));
				}
			}
		}.start();
	}
}
