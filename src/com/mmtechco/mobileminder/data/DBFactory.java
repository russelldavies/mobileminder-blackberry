package com.mmtechco.mobileminder.data;

import java.io.IOException;

import com.mmtechco.mobileminder.prototypes.FileDataWriter;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.util.ToolsBB;

public class DBFactory {
	private static final int sdkLegacy = 4; // SQLite part of 5 and above.
	private static ToolsBB tools = (ToolsBB) ToolsBB.getInstance();
	
	public static LocalDataWriter getLocalDataWriter() throws IOException {
		if (tools.getOSVersionGen() > sdkLegacy) {
			return new LocalDataAccess();
		} else {
			return new LocalDataAccess();
			//return new LocalDataAccessLegacy();
		}
	}

	public static FileDataWriter getFileDataWriter() throws IOException {
		if (tools.getOSVersionGen() > sdkLegacy) {
			return new FileDataAccess();
		} else {
			return new FileDataAccessLegacy();
		}
	}
}