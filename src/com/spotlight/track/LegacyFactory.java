package com.spotlight.track;

public class LegacyFactory implements LocalDataFactory {
	public LocalDataReader createLocalDataReader() {
        return new innerLegacyDataAccess();//return new LegacyLocalDataReader();
    }

}
