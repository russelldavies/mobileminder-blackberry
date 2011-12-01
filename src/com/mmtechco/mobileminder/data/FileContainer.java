package com.mmtechco.mobileminder.data;

/**
 * Container for file information.
 */
public class FileContainer {
	private boolean newFile; // Newly added to storage
	private boolean found; // File exists in the device storage
	private boolean sent;
	private String name;
	private String dir;
	private String path;
	private long modTime; // Last modfied time
	private long size; // In bytes
	private String md5;

	public FileContainer() {
		clearData();
	}

	public FileContainer(boolean newFile, boolean found, boolean sent,
			String name, String dir, String path, long modTime, long size) {
		this.newFile = newFile;
		this.found = found;
		this.sent = sent;
		this.name = name;
		this.dir = dir;
		this.path = path;
		this.modTime = modTime;
		this.size = size;
	}

	public void clearData() {
		newFile = false;
		found = false;
		sent = false;
		name = null;
		dir = null;
		path = null;
		modTime = 0;
		size = 0;
		md5 = null;
	}

	public String toString() {
		return "name: " + name + "; dir: " + dir + "; path: " + path
				+ "; modification time: " + modTime + "; size: " + size
				+ "; md5: " + md5;
	}

	public boolean isNewFile() {
		return newFile;
	}

	public boolean isFound() {
		return found;
	}

	public boolean isSent() {
		return sent;
	}

	public String getName() {
		return name;
	}

	public String getDir() {
		return dir;
	}

	public long getSize() {
		return size;
	}

	public String getPath() {
		return path;
	}

	public long getModTime() {
		return modTime;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
}
