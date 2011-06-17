package com.kids;

import com.kids.prototypes.LocalDataReader;

public class LegacyFactory implements LocalDataFactory {
	public LocalDataReader createLocalDataReader() {
        return new innerLegacyDataAccess();//return new LegacyLocalDataReader();
    }

}
