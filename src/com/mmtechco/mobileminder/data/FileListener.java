package com.mmtechco.mobileminder.data;

import com.mmtechco.mobileminder.sync.FileSync;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;

public class FileListener implements FileSystemJournalListener {
	private static final String TAG = ToolsBB.getSimpleClassName(FileListener.class);
	
	private static Logger logger = Logger.getInstance();
	private FileDb fileLog;
	
	private long lastUSN = 0;
	
	public FileListener(FileDb fileLog) {
		this.fileLog = fileLog;
	}

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
					logger.log(TAG, "File added: " + path);
					fileLog.add(path);
					break;
				case FileSystemJournalEntry.FILE_DELETED:
					logger.log(TAG, "File deleted: " + path);
					fileLog.delete(path);
					break;
				case FileSystemJournalEntry.FILE_RENAMED:
					logger.log(TAG, "File renamed: " + path);
					fileLog.renamed(path, "file://" + entry.getOldPath());
					break;
				case FileSystemJournalEntry.FILE_CHANGED:
					logger.log(TAG, "File changed: " + path);
					fileLog.changed(path);
					break;
				}
			}
		}
		lastUSN = nextUSN;
		
		fileLog.upload();
	}
}
