package com.spotlight.track;

/**
 * An interface which defines the structure of messages, which are being sent to server.
 *
 */
public interface Message 
{
	int 	getType();//call,text..
	String 	getTime();
	String 	getREST();//calls this.toString()
}
