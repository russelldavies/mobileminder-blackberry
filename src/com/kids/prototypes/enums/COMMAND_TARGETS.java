package com.kids.prototypes.enums;

public class COMMAND_TARGETS
{
	//private COMMAND_TARGETS(){}
	/**
	 * Sets the column name.
	 * @param inputColumnName name of the column
	 */
	public COMMAND_TARGETS(String inputColumnName)
	{	columnName = inputColumnName;	}
	
	
	public static final COMMAND_TARGETS SHOW	    = new COMMAND_TARGETS("Show");
	public static final COMMAND_TARGETS CALL	    = new COMMAND_TARGETS("Call");
	public static final COMMAND_TARGETS TEXT	    = new COMMAND_TARGETS("SMS");
	public static final COMMAND_TARGETS APP	    	= new COMMAND_TARGETS("App");
	public static final COMMAND_TARGETS WEB	    	= new COMMAND_TARGETS("Web");
	public static final COMMAND_TARGETS CONTACTS 	= new COMMAND_TARGETS("Conts");
	public static final COMMAND_TARGETS FILES    	= new COMMAND_TARGETS("files");
	public static final COMMAND_TARGETS OWNER    	= new COMMAND_TARGETS("owner");
	
	//private static final String values[]= {"Show","Call","SMS","App","Web","Conts","files"};
/*
	public static final String SHOW	    = "Show";
	public static final String CALL	    = "Call";
	public static final String TEXT	    = "SMS";
	public static final String APP	    = "App";
	public static final String WEB	    = "Web";
	public static final String CONTACTS = "Conts";
	public static final String FILES    = "files";
	public static final String OWNER    = "owner";
	*/
	private String columnName			= "";
	
	
	/**
	 * Converts column name to String type.
	 */
	//@Override
	public String toString()
	{	return columnName;	}
	
	/**
	 * Finds the enum in string format against input string and return it as enum constant.
	 * @param inputText input string
	 * @return enum of type COMMAND_TARGETS if it is found, null otherwise.
	 */
	public static COMMAND_TARGETS from(String inputText)
	{
		COMMAND_TARGETS tar = null;
					
		
		if (inputText.equalsIgnoreCase(SHOW.toString()))
		{tar = new COMMAND_TARGETS("Show"); }
		else if (inputText.equalsIgnoreCase(CALL.toString()))
		{tar = new COMMAND_TARGETS("Call");	}
		else if (inputText.equalsIgnoreCase(TEXT.toString()))
		{tar = new COMMAND_TARGETS("Text"); }
		else if (inputText.equalsIgnoreCase(APP.toString()))
		{tar = new COMMAND_TARGETS("App");	}
		else if (inputText.equalsIgnoreCase(WEB.toString()))
		{tar = new COMMAND_TARGETS("Web");	}
		else if (inputText.equalsIgnoreCase(CONTACTS.toString()))
		{tar = new COMMAND_TARGETS("Conts"); }
		else if (inputText.equalsIgnoreCase(FILES.toString()))
		{tar = new COMMAND_TARGETS("files"); }
		else if (inputText.equalsIgnoreCase(OWNER.toString()))
		{tar = new COMMAND_TARGETS("owner"); }
		
		return tar;
	}	
}