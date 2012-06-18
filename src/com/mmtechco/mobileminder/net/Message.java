package com.mmtechco.mobileminder.net;

import java.util.Enumeration;
import java.util.Vector;

import com.mmtechco.mobileminder.Registration;

public class Message {
	public static final String SEPARATOR = ",";
	
	public static final int COMMAND = 0;
	public static final int CALL = 1;
	public static final int SMS = 2;
	public static final int MAIL = 3;
	public static final int WEB_HISTORY = 4;
	public static final int APP_USAGE = 5;
	public static final int LOCATION = 6;
	public static final int ERROR = 7;
	public static final int REGISTRATION = 9;
	public static final int CALL_SYNC = 11;
	public static final int SMS_SYNC = 12;
	public static final int FILE = 22;
	public static final int CONTACT_PIC = 28;
	public static final int BOOT_UP = 31;
	public static final int UNINSTALL = 33;
	public static final int TIME_CHANGE = 34;
	
	private Vector fields;
	private int type;
	
	public Message(int type) {
		this.type = type;
		fields = new Vector();
	}
	
	public Message(int type, final String[] fields) {
		this.type = type;
		this.fields = new Vector();
		for (int i = 0; i < fields.length; i++) {
			this.fields.addElement(fields[i]);
		}
	}
	
	public Message(int type, Vector fields) {
		this.type = type;
		this.fields = fields;
	}
	
	public void add(String field) {
		fields.addElement(field);
	}
	
	public String toString() {
		StringBuffer request = new StringBuffer();
		
		// All messages start with "id,type,"
		request.append(Registration.getRegID());
		request.append(SEPARATOR);
		request.append(String.valueOf(type));
		request.append(SEPARATOR);
		
		// Add other custom fields
		for(Enumeration enum = fields.elements(); enum.hasMoreElements(); ) {
			request.append((String) enum.nextElement());
			request.append(SEPARATOR);
		}
		
		// Strip off last separator
		request.deleteCharAt(request.length() - 1);
		
		return request.toString();
	}
}
