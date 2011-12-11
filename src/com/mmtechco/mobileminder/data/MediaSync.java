package com.mmtechco.mobileminder.data;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.database.DatabaseException;

import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;
import com.mmtechco.mobileminder.util.ErrorMessage;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.MMLinkedList;
import com.mmtechco.mobileminder.util.MMLinkedListIterator;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Used to retrieve, edit and reference files on the device file system
 */
public class MediaSync extends Thread implements Controllable {
	private static final String TAG = ToolsBB
			.getSimpleClassName(MediaSync.class);

	private LocalDataAccess actLog;
	private FileInfoManager fileInfoManager;
	public static boolean mobileSync = false;
	private final int syncTime = 1000 * 10;// 60 * 10;
	// private ArrayList <String> fileDeleteQueue = new ArrayList<String>();
	private static MMLinkedList fileDeleteQueue = new MMLinkedList();
	private boolean isImage = false;
	private boolean inDirList = false;
	private Logger logger = Logger.getInstance();

	// Special case to use specific methods.
	private ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	private boolean synced = false;
	private boolean sleeping = false;
	private boolean bootSync = true;
	private int directoryCount = 0;
	private String rootDir;

	public MediaSync(LocalDataWriter localData) throws DatabaseException,
			IOException {
		fileInfoManager = new FileInfoManager(localData);
		logger.log(TAG, "Started");
		if (ToolsBB.fsMounted(FILESYSTEM.SDCARD)) {
			rootDir = "file:///SDCard/";
		} else {
			rootDir = "file:///store/home/user/";
		}
	}

	/**
	 * Sync between the device file system and the local file information
	 * storage.
	 */
	public void run() {
		logger.log(TAG, "Running");

		sleeping = true;
		if (!bootSync) {
			logger.log(TAG, "MediaSync Sleeping");
			try {
				Thread.sleep(syncTime);
			} catch (InterruptedException e1) {
				actLog.addMessage(new ErrorMessage(e1));
			}
			logger.log(TAG, "MediaSync Waking Up");
		}

		synced = false;
		int passes = 0;
		while (true != synced) {
			passes++;
			try {
				logger.log(TAG, "startpass...");
				fileInfoManager.startPass();
				logger.log(TAG, "digging dir...");
				// digDir(Environment.getExternalStorageDirectory());
				digDir(rootDir);
				logger.log(TAG, "endpass...");
				fileInfoManager.endPass();
				logger.log(TAG, "delete queue...");
				deleteFile();
				synced = true;
			} catch (Exception e) {
				logger.log(TAG, "Error thrown MediaSync thread: " + e);
				actLog.addMessage(new ErrorMessage(e));
				synced = false;
			}

			if (3 == passes) {
				synced = true;
				this.interrupt();
				break;
			}
		}
		sleeping = false;
		bootSync = false;
	}

	/**
	 * This method stores the path of a file to be deleted.
	 * 
	 * @param _file
	 *            file path
	 * @return true if file was stored
	 */
	public boolean queueDeleteFile(String _file) {
		logger.log(TAG, "File path in delete queue" + _file);
		fileDeleteQueue.add(_file);
		return true;
	}

	/**
	 * This method recursively deletes the file(s) listed in the delete queue
	 * based on their path.
	 */
	public void deleteFile() {
		try {
			Thread deleteFileProcess = new Thread() {
				public void run() {
					boolean deleted = false;
					// file(s) to be deleted
					if (!(0 == fileDeleteQueue.size())) {
						// ArrayList <String> fileNOTDeleteQueue = new
						// ArrayList<String>();
						MMLinkedList fileNOTDeleteQueue = new MMLinkedList();
						FileConnection fileToDelete;
						// Iterator<String> itr = fileDeleteQueue.iterator();

						for (MMLinkedListIterator theItr = fileDeleteQueue
								.getIterator(); theItr.isValid(); theItr
								.advance()) {
							String pathToDelete = (String) theItr.retrieve();
							// check for duplicate file
							String[] rtnPaths = fileInfoManager
									.findDuplicate(pathToDelete);
							logger.log(TAG, "Dupliacte Path Array Size: "
									+ rtnPaths.length);
							// delete each file in duplicate array
							for (int count = 0; count < rtnPaths.length; count++) {
								try {
									fileToDelete = (FileConnection) Connector
											.open(rtnPaths[count],
													Connector.READ_WRITE);

									if (!fileToDelete.exists()) {
										logger.log(TAG, "File: "
												+ rtnPaths[count]
												+ " doesn't exist");
									} else {
										fileToDelete.delete();
										if (deleted) {
											logger.log(TAG, "File: "
													+ rtnPaths[count]
													+ " has been deleted");
										} else// can't perform deletion
										{
											logger.log(TAG, "File: "
													+ rtnPaths[count]
													+ " has NOT been deleted");
											fileNOTDeleteQueue
													.add(rtnPaths[count]);
										}
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						// queue not deleted files
						fileDeleteQueue = fileNOTDeleteQueue;
					}
				}
			};
			deleteFileProcess.start();
		} catch (Exception e) {
			e.printStackTrace();
			actLog.addMessage(new ErrorMessage(e));
		}
	}

	/**
	 * This method digs through the file system and retrieves all the files
	 * 
	 * @param f
	 *            URI to the file system
	 * @return ArrayList of all the files in the system
	 */
	private void digDir(String fileName) {
		// TODO: implement
	}

	public boolean processFileCommand(String args) {
		try {
			if (!sleeping) {
				run();
			}
		} catch (Exception e) {
			actLog.addMessage(new ErrorMessage(e));
		}
		return synced;
	}

	private void generateMd5(String fileName) {
		/*
		FileConnection file = (FileConnection)Connector.open(fileName, Connector.READ_WRITE);
		MD5Digest md;
		DigestInputStream dis = null;
		try {
			// Calculate the digest for the given file.
			dis = new DigestInputStream(md, file);
			byte[] buffer = new byte[8192];
			while (dis.read(buffer) != -1)
				;
			byte[] digest = md.digest();
			// Convert to hexstring
			String md5 = "";
			for (int i = 0; i < digest.length; i++) {
				md5 += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			logger.log(TAG, "Generated md5: " + md5);
			// Add to the file info manager to be stored
			fileInfoManager.setMd5(md5);
		} catch (Exception e) {
			actLog.addMessage(new ErrorMessage(e));
		} finally {
			try {
				if (dis != null) {
					dis.close();
				}
			} catch (IOException e) {
				actLog.addMessage(new ErrorMessage(e));
			}
		}
		*/
	}

	public boolean processCommand(String[] _inputArgs) {

		final String[] inputArgs = _inputArgs;

		Thread processingThread = new Thread() {
			public void run() {
				logger.log(TAG, "args[1]:Path = " + inputArgs[1]);

				if (inputArgs[0].equalsIgnoreCase("del")) {
					logger.log(TAG, "Processing Delete File Command...");
					// decode utf16 characters
					queueDeleteFile(new String(
							tools.safeRangeTextUTFDecode(inputArgs[1])));
				}// delete file

				// This will set the boolean value that specifies if the device
				// can sync photos over a mobile network connection
				if (inputArgs[0].equalsIgnoreCase("mobile")) {
					logger.log(TAG, "Processing Mobile Sync Command...");
					mobileSync = (inputArgs[1].equals("1") ? true : false);
					logger.log(TAG, "mobileSync is set to : " + mobileSync);
				}// sync files over mobile connection
			}
		};

		processingThread.start();
		processingThread = null;

		if (inputArgs[0].equalsIgnoreCase("del")) {
			try {
				if (!sleeping) {
					run();
					return true;
				}
			} catch (Exception e) {
				actLog.addMessage(new ErrorMessage(e));
			}
		} else if (inputArgs[0].equalsIgnoreCase("mobile")) {
			return true;
		}

		return false;

	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.FILES) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method has been overridden from the FileObservable interface. By
	 * implementing this interface this class can specify the type of commands
	 * it can process.
	 * 
	 * @param target
	 *            passed to be checked.
	 * @return true if this is the desired target.
	 */
	/*
	public boolean isTarget(OBSERVERS targets) {
		if (targets == OBSERVERS.FILECHANGES) {
			return true;
		} else {
			return false;
		}
	}
	*/
}
