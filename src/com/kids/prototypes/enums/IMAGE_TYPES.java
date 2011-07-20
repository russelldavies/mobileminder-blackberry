package com.kids.prototypes.enums;

/**
 * Used to reference valid image types.
 */
public class IMAGE_TYPES
{
	private IMAGE_TYPES(){}
	/*	
    public static final byte GIF     = 1;
    public static final byte PNG     = 2;
    public static final byte JPEG    = 3;
    public static final byte WBMP    = 4;
    public static final byte BMP     = 5;
    public static final byte TIFF    = 6;
    public static final byte ICO     = 9;
	*/
   	public static final String UNKNOWN	= "UNKNOWN";	
   	public static final String GIF		= "GIF";
	public static final String PNG		= "PNG";
	public static final String JPEG		= "JPEG";
	public static final String WBMP		= "WBMP";
	public static final String BMP		= "BMP";
	public static final String TIFF		= "TIFF";
	public static final String ICO		= "ICO";
	//EXTRAS - Probably not used
	public static final String JFIF		= "JFIF";
	public static final String PSD		= "PSD";
	public static final String PSB		= "PSB";
	public static final String PSP		= "PSP";
	public static final String DNG		="DNG";
	public static final String RAW		="RAW";
     
	
	private String columnName;
	
	/**
	 * Sets the image type name as String type.
	 * @param inputColumnName String image type name.
	 */
	private IMAGE_TYPES(String inputColumnName)
	{	columnName = inputColumnName;	}
	
	/**
	 * Converts image type name to String.
	 */
	//@Override
	public String toString()
	{	return columnName;	}

}