package com.mmtechco.mobileminder.data;

/**
 * This class is used to store files.
 * 
 * <p>Fields (use accessors and mutators):</p>
 * <p>newFile - specifies if the file is newly added to the storage.</p>
 * <p>found - specifies if the file exists in the device storage.</p>
 * <p>name - specifies the file name.</p>
 * <p>path - specifies the file path.</p>
 * <p>lastModifiedTime - specifies the file's last modification time.</p>
 * <p>size - specifies the file size in bytes.</p>
 * <p>md5 - specifies the md5 value calculated using the file stream.</p>
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

	public FileInfo() {
		clearData();
	}

	public void clearData() {
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
	
	/**
	 * Generated Getters and Setters for the files attributes
	 */
	public boolean isNewFile() {
		return newFile;
	}

	public void setNewFile(boolean newFile) {
		this.newFile = newFile;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public long getSize() {
		return sizeInBytes;
	}

	public void setSize(long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
