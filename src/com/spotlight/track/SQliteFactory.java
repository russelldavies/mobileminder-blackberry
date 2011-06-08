package com.spotlight.track;

import com.kids.prototypes.LocalDataReader;

public class SQliteFactory implements LocalDataFactory {
	public LocalDataReader createLocalDataReader() {
        return new SQliteLocalDataReader();
    }

}
