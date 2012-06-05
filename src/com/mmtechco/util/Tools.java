package com.mmtechco.util;

import java.util.Date;
import java.util.Random;

import com.mmtechco.mobileminder.prototypes.MMTools;

/**
 * Cross platform tools that can be used anywhere in the app.
 * 
 */
public abstract class Tools implements MMTools {
	protected static final String TAG = ToolsBB.getSimpleClassName(Tools.class);
	
	protected Logger logger = Logger.getInstance();
	private final String charSet = "!$&()*+-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]_abcdefghijklmnopqrstuvwxyz~";

	public String getDate() {
		return getDate(new Date().getTime());
	}
	
	public int strToInt(String inputString) {
		try {
			return Integer.parseInt(inputString.trim());
		} catch (Exception e) {
			return -1;
		}
	}

	public synchronized boolean isNumber(String num) {
		try {
			Integer.parseInt(num);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public boolean containsOnlyNumbers(String num) {
		// Can't be used because BB doesn't support regexes
		// Regex matches more or more digits. Null check is to prevent
		// exceptions
		// if (num != null && num.matches("\\d+")) {

		// It can't contain only numbers if it's null or empty
		if (num == null || num.length() == 0) {
			return false;
		}
		for (int i = 0; i < num.length(); i++) {
			// If we find a non-digit character we return false.
			if (!Character.isDigit(num.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean isHex(String inputString) {
		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);
			if (Character.isDigit(c)
					|| (("0123456789abcdefABCDEF".indexOf(c)) >= 0)) {
				return true;
			}
		}
		return false;
	}

	public String stringToHex(String inputString) {
		char[] b;
		b = inputString.toCharArray();
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}
	
	public String hexToString(String hex) {
		StringBuffer output = new StringBuffer();
		String str = "";
		for (int i = 0; i < hex.length(); i += 2) {
			str = hex.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}
		return output.toString();
	}

	public String topAndTail(String hexString) {
		Random rand = new Random();
		int top = rand.nextInt(16);
		int tail = rand.nextInt(16);
		String hexTop = Integer.toHexString(top).toUpperCase();
		String hexTail = Integer.toHexString(tail).toUpperCase();
		// Top=======Hex_string=======tail
		hexTop = hexTop.concat(hexString).concat(hexTail);
		// logger.log("topAndTail:returns:"+hexTop);
		return hexTop;
	}

	public String reverseTopAndTail(String hexString) {
		String returnString = hexString.substring(1, (hexString.length() - 1));
		return returnString;
	}

	public String safeRangeTextUTF(String inputString) {
		// Possible regression: was StringBuilder
		// StringBuilder builder = new StringBuilder();
		StringBuffer builder = new StringBuffer();
		char[] text = inputString.toCharArray();
		String prefix = "&#";
		//logger.log(TAG, "Before Safe Range Encoding: " + inputString);

		for (int count = 0; count < text.length; count++) {
			if (255 < text[count]) {
				builder.append(prefix);
				builder.append(Integer.parseInt(
						Integer.toHexString(text[count]), 16));
				builder.append(';');
			} else {
				builder.append(text[count]);
			}
		}
		//logger.log(TAG, "After Safe Range Encoding: " + builder.toString());

		return builder.toString();
	}

	public String safeRangeTextUTFDecode(String inputString) {
		if (inputString.indexOf("&#") == -1) {
			return inputString;
		}
		// Possible regression: was StringBuilder
		// StringBuilder builder = new StringBuilder();
		StringBuffer builder = new StringBuffer();
		char[] text = inputString.toCharArray();

		for (int count = 0; count < text.length; count++) {
			if ('&' == text[count] && '#' == text[1 + count]) {
				int endpoint = inputString.indexOf(";", count);
				String intString = inputString.substring(2 + count, endpoint);
				builder.append((char) Integer.parseInt(intString));
				count = endpoint;
			} else {
				builder.append(text[count]);
			}
		}
		return builder.toString();
	}

	public String getRandomString(int length) {
		// SecureRandom would be better but speed issues not tested
		Random random = new Random();
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = charSet.charAt(random.nextInt(charSet.length()));
		}
		return text.toString();
	}
}
