package com.kids;

public class COMMAND_TARGETS {
	private COMMAND_TARGETS(){}

	public static final String SHOW	    = "Show";
	public static final String CALL	    = "Call";
	public static final String TEXT	    = "SMS";
	public static final String APP	    = "App";
	public static final String WEB	    = "Web";
	public static final String CONTACTS = "Conts";
	public static final String FILES    = "files";
	
	//private static final String values[]= {"Show","Call","SMS","App","Web","Conts","files"};
	
	private String columnName			= "";
	
	/**
	 * Sets the column name.
	 * @param inputColumnName name of the column
	 */
	private COMMAND_TARGETS(String inputColumnName)
	{	columnName = inputColumnName;	}
	
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
	public static String from(String inputText)//COMMAND_TARGETS from(String inputText)
	{
		if(null!=inputText)
		{
			String tar;
			for(int count=0;;count++)
			{
				tar = inputText;
				if(inputText.equalsIgnoreCase(tar.toString()))
				{ return tar;	}	
			}
			/* NO FOR:EACH IN BLACKBERRY
			for(COMMAND_TARGETS tar : COMMAND_TARGETS.values())
			{
				if(inputText.equalsIgnoreCase(tar.toString()))
				{ return tar;	}
			}*/
		}
		return null;
	}	
}