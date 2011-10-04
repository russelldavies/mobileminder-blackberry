package com.kids.prototypes;

public interface MMTools 
{
		
		public int getUptimeInSec();
		/**
		 * This method converts a safely converts a long integer into a standard integer	
		 * @param l long integer
		 * @return standard version
		 */
		public int safeLongToInt(long l);
		

		/**
		 * Gets the correct format of date in specified format.
		 * @param _date milliseconds since the epoch.
		 * @return Date in milliseconds value in specified format.
		 * @throws ParseException 
		 * @throws throws ParseException when input date format is not correct.
		 */	
		public long getDate(String _date); //throws ParseException;
		
		/**
		 * Gets the string format of date in specified format.
		 * @param _date milliseconds since the epoch.
		 * @return
		 */
		public String getDate(long _date);
		
		/**
		 * Gets the String format of date.
		 * @return string which contains the date.
		 */
		public String getDate();
		

	   public String calcHM(long timeInSeconds);
	         
		
		/**
		 * Converts a number which in string format to integer format.
		 * @param _text a number which in string format.
		 * @return a number which in integer format.
		 */
		public int txt2num(String _text);
		/**
		 * Checks the inputNumber to check whether it matches the number pattern.
		 * @param _text a number.
		 * @return boolean true if number matches with the pattern false otherwise.
		 */
		public boolean isNumber(String _text);
		
		/**
		 * This method formats a string into a hex string
		 * 
		 * @param b string
		 * @return hex string
		 */
		 public String stringToHex(String s);
		
		/**
		 * This method formats a HEX string, adding a random HEX value to the start and end of the string
		 * 
		 * @param _input HEX string
		 * @return hex string
		 */
		public String topAndTail(String _input);
		
		/**
		 * This method formats a HEX string, removing a random HEX value from the start and end of the string
		 * 
		 * @param _input HEX string
		 * @return hex string
		 */
		public String reverseTopAndTail(String _input);

		 /**
		 * Return true if the argument string seems to be a
		 * Hex data string, like "a0 13 2f ". Whitespace is
		 * ignored.
		 */
		public boolean isHex(String _sampleData);
		
		public int charOccurence(String _str, char _char);
}
