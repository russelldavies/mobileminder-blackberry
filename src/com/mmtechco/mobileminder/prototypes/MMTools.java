package com.mmtechco.mobileminder.prototypes;

public interface MMTools {
	/**
	 * Gets the time in second from when the device booted.
	 * 
	 * @return an integer representing the up-time in seconds.
	 */
	public long getUptimeInSec();

	/**
	 * Gets current date and time formatted.
	 * 
	 * @return Returns current date and time in format of YYMMDDHHMMSS+ZZ (where
	 *         'Z' is timezone).
	 */
	public String getDate();

	/**
	 * Given a date in specified format, returns the epoch time of that date.
	 * 
	 * @param date
	 *            - a date string in format of YYMMDDHHMMSS+ZZZZ (where 'Z' is
	 *            timezone).
	 * @return The date's epoch time in <strong>milliseconds</strong>.
	 * @throws Exception
	 *             generic exception (instead of ParseException) for BlackBerry
	 *             compatibility reasons when input date format is not correct.
	 */
	public long getDate(String date) throws Exception;

	/**
	 * Takes an date in epoch and returns it formatted.
	 * 
	 * @param date
	 *            - a date value in epoch time in <strong>milliseconds</strong>.
	 * @return date string in YYMMDDHHMMSS+ZZ format (where 'Z' is timezone).
	 */
	public String getDate(long date);

	/**
	 * Safely converts a number in string format to integer format.
	 * 
	 * @param inputString
	 *            - a number which is in string format.
	 * @return the number converted to an integer or -1 if conversion
	 *         unsuccessful.
	 */
	public int strToInt(String num);

	/**
	 * Checks the inputNumber to check whether it matches the number pattern.
	 * 
	 * @param inputString
	 *            - a number as String type.
	 * @return boolean true if number matches with the pattern false otherwise.
	 */
	public boolean isNumber(String inputString);

	/**
	 * This method ensures that a string only contains numbers
	 * 
	 * @param inputString
	 *            the string to be checked
	 * @return true if the string contains a non-number character
	 */
	public boolean containsOnlyNumbers(String num);

	/**
	 * Return true if the argument string seems to be a Hex data string, like
	 * "a0 13 2f ". Whitespace is ignored.
	 * 
	 * @param s
	 *            - string to be tested
	 */
	public boolean isHex(String inputString);

	/**
	 * Formats a string into a hex string
	 * 
	 * @param inputString
	 *            - string to be converted
	 * @return hex converted string
	 */
	public String stringToHex(String inputString);

	/**
	 * Formats a hex string into a String
	 * 
	 * @param hex
	 *            - hex string
	 * @return un-hexed string
	 */
	public String hexToString(String hex);

	/**
	 * Formats a hex string, adding a random hex value to the start and end of
	 * the string
	 * 
	 * @param input
	 *            - hex string
	 * @return hex string
	 */
	public String topAndTail(String hexString);

	/**
	 * Formats a hex string, removing a random hex value from the start and end
	 * of the string
	 * 
	 * @param input
	 *            - hex string
	 * @return hex string
	 */
	public String reverseTopAndTail(String hexString);

	/**
	 * Ensures that all characters in the array are web transmission safe. The
	 * hex delimiter is the value 128 represented as a character.
	 * 
	 * @param inputString
	 *            - input char array
	 * @return web transmission safe char array
	 */
	public String safeRangeTextUTF(String inputString);

	/**
	 * Ensures that all characters in the string are web transmission safe. The
	 * hex delimiter is the value 128 represented as a character. This method
	 * decodes the encoded string
	 * 
	 * @param inputString
	 *            - input string
	 * @return web transmission safe string
	 */
	public String safeRangeTextUTFDecode(String inputString);
	
	/**
	 * Creates a string of desired length containing the character set randomly
	 * distributed.
	 * 
	 * @param length
	 *            - the length of the random character set to be created.
	 * @return a string with random characters.
	 */
	public String getRandomString(int length);
	
	/**
	 * Gets the manufacturer information of the device.
	 * @return String of manufacturer otherwise an "(unavailable)".
	 */
	public String getDeviceManufacturer();

	public String[] split(String strString, String strDelimiter);
}
