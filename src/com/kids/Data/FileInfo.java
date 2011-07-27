package com.kids.Data;

/**
 * This class is used to store files.
 */
public class FileInfo {

	private boolean newFile;
	private boolean found;
	private boolean sent;
	private String name;
	private String directory;
	private String path;
	private long lastModifiedTime;
	private long sizeInBytes;
	private String md5;
	
	public FileInfo()
	{clearData();}
	
	public void clearData()
	{
		newFile = false;
		found = false;
		sent = false;
		name = null;
		directory = null;
		path = null;
		lastModifiedTime = 0;
		sizeInBytes = 0;
		md5 = null;
	}
/*
/**
 * This method constructs the file, setting its attributes
 * 
 * @param _newFile specifies if the file is newly added to the storage
 * @param _found specifies if the file exists in the device storage
 * @param _name specifies the file name
 * @param _path specifies the file path
 * @param _lastModifiedTime specifies the file's last modification time.
 * @param _size specifies the file size in bytes
 * @param _md5 specifies the md5 value calculated using the file stream.

	public FileInfo(boolean _newFile, boolean _found, String _name, String _path, long _lastModifiedTime, long _size, String _md5)
	{
	}
	*/
/**
 * Generated Getters and Setters for the files attributes
 */
	public boolean isNewFile() 
	{return newFile;}
	public void setNewFile(boolean newFile) 
	{this.newFile = newFile;}
	public boolean isFound() 
	{return found;}
	public void setFound(boolean found) 
	{this.found = found;}
	public boolean isSent() 
	{return sent;}
	public void setSent(boolean sent) 
	{this.sent = sent;}
	public String getName() 
	{return name;}
	public void setName(String name)
	{this.name = name;}
	public String getDirectory() 
	{return directory;}
	public void setDirectory(String directory)
	{this.directory = directory;}
	public String getPath() 
	{return path;}
	public void setPath(String path) 
	{this.path = path;}
	public long getLastModifiedTime() 
	{return lastModifiedTime;}
	public void setLastModifiedTime(long lastModifiedTime) 
	{this.lastModifiedTime = lastModifiedTime;}
	public long getSize() 
	{return sizeInBytes;}
	public void setSize(long sizeInBytes) 
	{this.sizeInBytes = sizeInBytes;}
	public String getMd5() 
	{return md5;}
	public void setMd5(String md5) 
	{this.md5 = md5;}

}