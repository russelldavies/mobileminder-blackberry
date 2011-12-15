package com.mmtechco.mobileminder.util;

/**
 * Indicates that there is a problem with the device's storage.
 */
public class StorageException extends Exception {
	public StorageException(String s) {
		super(s);
	}
}
