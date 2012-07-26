package com.mmtechco.mobileminder.data;

public class FILESYSTEM {
	private String name;
	
	private FILESYSTEM() {
	}
	
	private FILESYSTEM(String name) {
		this.name = name;
	}
	
	public static final FILESYSTEM SDCARD = new FILESYSTEM("sdcard");
	public static final FILESYSTEM SYSTEM = new FILESYSTEM("system");
	public static final FILESYSTEM STORE = new FILESYSTEM("store");
	
	public String toString() {
		return name;
	}
}
