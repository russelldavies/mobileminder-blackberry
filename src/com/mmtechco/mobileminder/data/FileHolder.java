package com.mmtechco.mobileminder.data;

import net.rim.device.api.util.Persistable;

/**
 * Helper class to store information about directories and files that are being
 * read from the system. Using this class prevents needless opening of the file
 * using FileConnection to check whether it is a file or directory.
 */
public final class FileHolder implements Persistable {
	private String path;
	private long modTime;
	private String md5;
	private boolean uploaded;

	/**
	 * Creates a new FileHolder object
	 * 
	 * @param fullPath
	 *            The path and file name provided from the FileConnection.
	 */
	public FileHolder(String fullPath, long modTime, String md5) {
		this.path = fullPath;
		this.modTime = modTime;
		this.md5 = md5;
		uploaded = false;
	}

	/**
	 * Retrieves the file name
	 * 
	 * @return Name of the file
	 */
	public String getFileName() {
		int slash = path.lastIndexOf('/');
		return path.substring(++slash);
	}

	/**
	 * Retrieves the path of the file
	 * 
	 * @return Fully qualified path of the file
	 */
	public String getPath() {
		return path;
	}
	
	public void setPath(String newPath) {
		this.path = newPath;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}

	public long getModTime() {
		return modTime;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getMd5() {
		return md5;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}

	public boolean isUploaded() {
		return uploaded;
	}
}
