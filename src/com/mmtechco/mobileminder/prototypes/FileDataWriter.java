package com.mmtechco.mobileminder.prototypes;

import com.mmtechco.mobileminder.data.FileContainer;

public interface FileDataWriter {
	void clean();
	void add(FileContainer file);
	void delete(int id);
}
