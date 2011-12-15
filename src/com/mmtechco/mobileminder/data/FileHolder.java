package com.mmtechco.mobileminder.data;

/**
 * Helper class to store information about directories and files that are being
 * read from the system. Using this class prevents needless opening of the file
 * using FileConnection to check whether it is a file or directory.
 */
public final class FileHolder {
	private String filename;
	private String path;
	private boolean isDir;

	/**
	 * Creates a new FileHolder object
	 * 
	 * @param fullPath
	 *            The path and file name provided from the FileConnection.
	 */
	public FileHolder(String fullPath, boolean isDir) {
		// Pull the information from the URI provided from the original
		// FileConnection.
		int slash = fullPath.lastIndexOf('/');
		if (slash == -1) {
			throw new IllegalArgumentException("full path must have a slash");
		}
		path = fullPath.substring(0, ++slash);
		filename = fullPath.substring(slash);
		this.isDir = isDir;
	}

	/**
	 * Retrieves the file name
	 * 
	 * @return Name of the file, or null if it's a directory
	 */
	public String getFileName() {
		return filename;
	}

	/**
	 * Retrieves the path of the directory or file
	 * 
	 * @return Fully qualified path of the file
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Determines if the FileHolder is a directory
	 * 
	 * @return true if FileHolder is directory, otherwise false
	 */
	public boolean isDirectory() {
		return isDir;
	}
}
