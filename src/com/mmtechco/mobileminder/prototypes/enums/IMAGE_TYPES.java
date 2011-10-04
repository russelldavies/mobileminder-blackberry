package com.mmtechco.mobileminder.prototypes.enums;

/**
 * Used to reference valid image types.
 */
public class IMAGE_TYPES {
	public static final String UNKNOWN = "UNKNOWN";
	public static final String GIF = "GIF";
	public static final String PNG = "PNG";
	public static final String JPEG = "JPEG";
	public static final String WBMP = "WBMP";
	public static final String BMP = "BMP";
	public static final String TIFF = "TIFF";
	public static final String ICO = "ICO";
	// EXTRAS - Probably not used
	public static final String JFIF = "JFIF";
	public static final String PSD = "PSD";
	public static final String PSB = "PSB";
	public static final String PSP = "PSP";
	public static final String DNG = "DNG";
	public static final String RAW = "RAW";

	private String columnName;
	
	private IMAGE_TYPES() {
	}

	/**
	 * Sets the image type name as String type.
	 * 
	 * @param inputColumnName
	 *            String image type name.
	 */
	private IMAGE_TYPES(String inputColumnName) {
		columnName = inputColumnName;
	}

	/**
	 * Converts image type name to String.
	 */
	public String toString() {
		return columnName;
	}

}