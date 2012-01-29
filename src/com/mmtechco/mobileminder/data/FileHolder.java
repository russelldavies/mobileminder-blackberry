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
	 * Create a new FileHolder object with the specified path, modification time
	 * and md5
	 * 
	 * @param fullPath
	 *            The path and file name provided from the FileConnection
	 * @param modTime
	 *            Unix time of when file was last modified
	 * @param md5
	 *            MD5 String hash of the file
	 * @throws IllegalArgumentException
	 *             if any of the parameters are null
	 */
	public FileHolder(String fullPath, long modTime, String md5) {
		if (fullPath == null || modTime == 0 || md5 == null) {
			throw new IllegalArgumentException();
		}
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

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof FileHolder) {
			FileHolder other = (FileHolder) obj;
			if (!other.path.equals(path))
				return false;
			if (!other.md5.equals(md5))
				return false;
			return true;
		}
		return false;
	}
}
