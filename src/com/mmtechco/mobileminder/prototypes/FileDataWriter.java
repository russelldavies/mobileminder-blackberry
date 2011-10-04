package com.mmtechco.mobileminder.prototypes;

import com.mmtechco.mobileminder.data.FileInfo;

public interface FileDataWriter {
	void clean();
	void add(FileInfo file);
	void delete(int id);
}
