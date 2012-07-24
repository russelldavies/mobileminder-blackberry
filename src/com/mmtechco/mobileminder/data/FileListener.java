package com.mmtechco.mobileminder.data;

import com.mmtechco.util.Logger;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;

public class FileListener implements FileSystemJournalListener {
	private static Logger logger = Logger.getLogger(FileListener.class);
	
	private long lastUSN = 0;
	
	public void fileJournalChanged() {
		long nextUSN = FileSystemJournal.getNextUSN();
		
		for (long currentUSN = nextUSN - 1; currentUSN >= lastUSN && currentUSN < nextUSN; --currentUSN) {
			FileSystemJournalEntry entry = FileSystemJournal.getEntry(currentUSN);
			
			// No entry was found
			if (entry == null) {
				break;
			}
			
			// Check if entry was added, deleted, or renamed
			String path = "file://" + entry.getPath();
			if (FileSync.supportedType(path)) {
				switch (entry.getEvent()) {
				case FileSystemJournalEntry.FILE_ADDED:
					logger.debug("File added: " + path);
					FileLog.add(path);
					break;
				case FileSystemJournalEntry.FILE_DELETED:
					logger.debug("File deleted: " + path);
					FileLog.delete(path);
					break;
				case FileSystemJournalEntry.FILE_RENAMED:
					logger.debug("File renamed: " + path);
					FileLog.renamed(path, "file://" + entry.getOldPath());
					break;
				case FileSystemJournalEntry.FILE_CHANGED:
					logger.debug("File changed: " + path);
					FileLog.changed(path);
					break;
				}
			}
		}
		lastUSN = nextUSN;
		
		// Upload the new files
		FileLog.upload();
	}
}
