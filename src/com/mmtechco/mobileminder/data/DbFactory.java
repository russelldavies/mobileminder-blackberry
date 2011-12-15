package com.mmtechco.mobileminder.data;

import com.mmtechco.mobileminder.util.StorageException;
import com.mmtechco.mobileminder.util.ToolsBB;

public class DbFactory {
	private static final int sdkLegacy = 4; // SQLite part of 5 and above.
	private static ToolsBB tools = (ToolsBB) ToolsBB.getInstance();
	
	public static LogDb getLocalDataWriter() throws StorageException {
		if (tools.getOSVersionGen() > sdkLegacy) {
			return new LogDb();
		} else {
			return new LogDb();
			//return new LocalDataAccessLegacy();
		}
	}

	public static FileDb getFileDataWriter(LogDb actLog) throws StorageException {
		if (tools.getOSVersionGen() > sdkLegacy) {
			return new FileDb(actLog);
		}// else {
			//return new FileDataAccessLegacy();
		//}
		return new FileDb(actLog);
	}
}