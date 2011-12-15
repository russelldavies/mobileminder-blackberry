package com.mmtechco.mobileminder.prototypes.enums;

/**
 * Used to reference valid image types.
 */
public class IMAGE_TYPES {
	public static final String JPG = "jpg";
	public static final String JPEG = "jpeg";
	public static final String PNG = "png";
	public static final String GIF = "gif";
	public static final String BMP = "bmp";
	public static final String WBMP = "wbmp";
	public static final String TIFF = "tiff";
	public static final String ICO = "ico";
	public static final String UNKNOWN = "UNKNOWN";
	// EXTRAS - Probably not used
	public static final String JFIF = "JFIF";
	public static final String PSD = "PSD";
	public static final String PSB = "PSB";
	public static final String PSP = "PSP";
	public static final String DNG = "DNG";
	public static final String RAW = "RAW";

	public static final String[] values = {JPG, JPEG, GIF};
	
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