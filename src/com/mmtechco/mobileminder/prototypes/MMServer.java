package com.mmtechco.mobileminder.prototypes;

import javax.microedition.io.file.*;
import com.mmtechco.mobileminder.net.Reply;

public interface MMServer {
	/**
	 * Sends a rest message to the server. It receives a reply message from the
	 * server.
	 * 
	 * @param inputMessage
	 *            - plan text massage in comma-separated values
	 * 
	 * @return a reply message from the server
	 */
	public Reply contactServer(Message inputMessage);

	public Reply contactServer(String inputMessage);

	public Reply contactServer(String inputBody, String crc, String pic);
	
	public Reply contactServer(String inputBody, FileConnection pic);
}