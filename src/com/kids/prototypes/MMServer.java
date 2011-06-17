package com.kids.prototypes;

import com.kids.net.Reply;

public interface MMServer {
	
	/**
	 * This method will sends a rest message to the server.
	 * It receives a reply message from the server.
	 * 
	 * @param inputMessage plan text massage in comma-separated values
	 * 
	 * @return a reply message from the server
	 */
	public Reply contactServer(Message inputMessage);

	public Reply contactServer(String inputMessage);

	public Reply contactServer(String inputBody, String crc, String pic);
	
	/**
	 * This method establishes the connection with the server.
	 */
	public void startup();
	
	/**
	 * This method terminates the connection with the server.
	 */
	public void shutdown();

	/**
	 * This method checks the status of the server.
	 * @return true if the server is live, otherwise is down.
	 */
	public boolean isLive();

	/**
	 * This method formats a hex string into a String
	 * 
	 * @param hex hex string 
	 * @return String
	 */
	 public String hexToString(String hex);
}
