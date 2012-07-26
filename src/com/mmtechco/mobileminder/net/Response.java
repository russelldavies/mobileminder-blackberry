package com.mmtechco.mobileminder.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import com.mmtechco.util.Logger;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.http.HttpProtocolConstants;

public class Response {
	private static Logger logger = Logger.getLogger(Response.class);
	
	int responseCode;
	String responseMessage;
	Hashtable headers;
	String content;

	public Response(HttpConnection connection) {
		try {
			responseCode = connection.getResponseCode();
			responseMessage = connection.getResponseMessage();

			// Get headers
			headers = new Hashtable(20);
			for (int i = 0; i < 20; i++) {
				String key = connection.getHeaderFieldKey(i);
				String field = connection.getHeaderField(i);
				if (key == null && i > 0) {
					break;
				}
				headers.put(key, field);
			}

			// Retrieve content
			InputStream input = connection.openInputStream();
			byte[] reply = IOUtilities.streamToBytes(input);
			content = new String(reply);
			input.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException e) {
					logger.error("Could not close connection");
				}
			}
		}
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public String getContent() {
		return content;
	}
	
	public Hashtable getHeaders() {
		return headers;
	}
	
	public String getWarning() {
		return (String) headers.get(HttpProtocolConstants.HEADER_WARNING);
	}
}
