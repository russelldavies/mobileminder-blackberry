package com.mmtechco.mobileminder.data;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseIOException;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.ErrorMessage;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Analyzes the device file system and relays any changes on to the local and
 * non local file system storage
 */
public class FileInfoManager {
	private static final String TAG = ToolsBB.getSimpleClassName(FileInfoManager.class);

	private FileDataAccess fileLog; // Local db for filesystem info
	private LocalDataWriter actLog;
	private FileContainer fileContainer = new FileContainer();
	private FileMessage flyFileMessage = new FileMessage();
	private Server server;
	private Logger logger = Logger.getInstance();
	private ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	public FileInfoManager(LocalDataWriter actLog) throws IOException, DatabaseIOException {
		this.actLog = actLog;
		fileLog = new FileDataAccess();
		fileLog.open();
		server = new Server(this.actLog);
	}

	/**
	 * This method prepares the db table for the the new file system changes to
	 * be added
	 */
	public void startPass() {
		if (0 != fileLog.length()) {
			logger.log(TAG, "filelog db not empty, cleaning");
			fileLog.clean();
		}
	}

	/**
	 * Sorts the files found in the system, passing new file information to
	 * fileLog to be recorded.
	 * @throws DatabaseException 
	 * @throws DataTypeException 
	 */
	public boolean addFile(FileConnection file) throws DataTypeException, DatabaseException {
		boolean preformMd5 = false;
		String name = file.getName();
		String directory = file.getPath();
		String path = file.getPath() + "/" + file.getName();
		long lastMod = file.lastModified();
		long size = file.totalSize();

		// initial files added
		// filelog is empty
		if (fileLog.length() == 0) {
			fileContainer = new FileContainer(true, true, false, name,
					directory, path, lastMod, size);
			fileLog.add(fileContainer);
			// set to true if the file need an MD5 value
			preformMd5 = fileContainer.isNewFile();
		} else {
			// cursor to a table consisting of name, path and size data for all
			// the files in the database
			Cursor processLog = fileLog.fileCheckTable();
			logger.log(TAG, "checking fileLog");
			boolean nameMatch = false;
			boolean pathMatch = false;
			boolean sizeMatch = false;

			processLog.first();
			// file check
			while (processLog.getPosition() != -1) {
				if (name.equals(processLog.getRow().getString(processLog.getColumnIndex(FileDataAccess.KEY_NAME)))) {
					nameMatch = true;
				}
				if (path.equals(processLog.getRow().getString(processLog.getColumnIndex(FileDataAccess.KEY_PATH)))) {
					pathMatch = true;
				}
				if (size == (processLog.getRow().getLong(processLog.getColumnIndex(FileDataAccess.KEY_SIZE)))) {
					sizeMatch = true;
				}
				processLog.next();
			}
			processLog.close();

			// filename, path or size have not changed
			if (nameMatch && pathMatch && sizeMatch == true) {
				logger.log(TAG, "MATCHED");
				preformMd5 = false;
				// clean fileContainer fly-weight object
				fileContainer.clearData();
				// logger.log(TAG, "File: "+_name+", matched in DB");
				fileLog.setFound(name);

				logger.log(TAG, "Matched: " + name + "namematched?: "
						+ nameMatch);
			} else {
				logger.log(TAG, "No Matched: " + name);
				fileContainer = new FileContainer(true, true, false, name,
						directory, path, lastMod, size);

				// add the file to the file-table
				fileLog.add(fileContainer);
				// set to true if the file need an MD5 value
				preformMd5 = fileContainer.isNewFile();
				// clean fileContainer fly object
				fileContainer.clearData();
			}
		}
		return preformMd5;
	}

	/**
	 * This method sorts and updates the local database of file system
	 * information matching it to the device file system. This new file system
	 * information & structure is then relayed on to the server.
	 * @throws DatabaseException 
	 * @throws DataTypeException 
	 * @throws IOException 
	 */
	public void endPass() throws DatabaseException, DataTypeException, IOException {
		final String add = "ADD";
		final String update = "UPD";
		final String delete = "DEL";

		// check for initial sync
		Reply resultREST;
		String fileName;
		String fileDir;
		String _path;
		int cursorCount;
		Cursor fileTable = fileLog.getAll();
		boolean initialSync = true;
		fileTable.last();
		cursorCount = fileTable.getPosition();
		fileTable.first();
		while (fileTable.getPosition() != -1 && 0 != cursorCount) {
			if (0 == (fileTable.getRow().getInteger(fileTable.getColumnIndex("new"))))
				;
			{
				initialSync = false;
			}
			fileTable.next();
			cursorCount--;
		}

		if (fileTable != null) {
			fileTable.close();
		}

		if (initialSync) {
			logger.log(TAG, "Initial Sync Sending Files...");

			Cursor fileTable2 = fileLog.getAll();
			fileTable2.last();
			cursorCount = fileTable2.getPosition();

			fileTable2.first();
			while (fileTable2.getPosition() == -1 && 0 != cursorCount) {
				fileName = fileTable2.getRow().getString(fileTable2 .getColumnIndex("name"));
				fileDir = fileTable2.getRow().getString(fileTable2
						.getColumnIndex("directory"));
				_path = fileTable2.getRow().getString(fileTable2.getColumnIndex("path"));
				FileConnection file = (FileConnection)Connector.open(fileName, Connector.READ_WRITE);

				try {
					Thread.sleep(1000 * 1 * 10);
				} // pause to aid garbage collection
				catch (InterruptedException e) {
					actLog.addMessage(new ErrorMessage(e));
				}

				if (null != file) {
					flyFileMessage.setMessage(add, _path, fileTable2.getRow().getLong(fileTable2.getColumnIndex("time")),
							fileTable2.getRow().getString(fileTable2 .getColumnIndex("md5")));

					logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
							+ flyFileMessage.getREST());
					// send file message
					logger.log(TAG, "Adding File Message..."); // set CRC
					resultREST = server.contactServer(flyFileMessage.getREST(),
							file);

					if (resultREST.isError()) {
						logger.log(TAG, "Error Sending File Post Message");
					} else {
						fileLog.setSent(_path);
					}
					flyFileMessage.clearData();
				} else {
					logger.log(TAG, "File is null");
				}
				fileTable2.next();
				cursorCount--;
			}
			if (fileTable2 != null) {
				fileTable2.close();
			}

		} else {
			/**
			 * Checking for file edited
			 */
			logger.log(TAG, "fileedit");
			// check if file was edited
			logger.log(TAG, "checking if file was edited...");
			Cursor notFoundTable = fileLog.getNotFoundFileTable();
			Cursor newTable = fileLog.getNewFileTable();
			newTable.last();
			cursorCount = newTable.getPosition();
			flyFileMessage.clearData();
			// newTable loop
			newTable.first();
			notFoundTable.first();
			while (newTable.getPosition() == -1 && 0 != cursorCount) {
				// notFoundTable loop
				while (notFoundTable.getPosition() == -1) {
					// check for name and path match
					if (newTable.getRow().getString(newTable.getColumnIndex("name"))
							.equals(notFoundTable.getRow().getString(notFoundTable .getColumnIndex("name")))
							&& newTable.getRow().getString( newTable.getColumnIndex("path")).equals(
									notFoundTable.getRow().getString(notFoundTable
											.getColumnIndex("path")))) {
						if (!newTable.getRow().getString(newTable.getColumnIndex("md5"))
								.equals(notFoundTable.getRow().getString(notFoundTable
										.getColumnIndex("md5")))) {
							logger.log(TAG, "file has been edited");
							fileLog.updateMd5(
									notFoundTable.getRow().getInteger(notFoundTable
											.getColumnIndex("_id")), newTable.getRow().getString(newTable
													.getColumnIndex("md5")),
									newTable.getRow().getLong(newTable
											.getColumnIndex("time")), newTable.getRow().getLong(newTable
													.getColumnIndex("size")));

							fileLog.delete(newTable.getRow().getInteger(newTable .getColumnIndex("_id")));
						}

						// add delete message
						flyFileMessage.setMessage(delete, newTable.getRow().getString(newTable.getColumnIndex("path")), 0,
								newTable.getRow().getString(newTable
										.getColumnIndex("md5")));

						logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
								+ flyFileMessage.getREST());
						// logger.log(TAG, "ADDING FILE DATA");

						// send file message
						logger.log(TAG, "Adding File Message...");
						actLog.addMessage(flyFileMessage);
						flyFileMessage.clearData();

						fileName = newTable.getRow().getString(newTable
								.getColumnIndex("name"));
						fileDir = newTable.getRow().getString(newTable
								.getColumnIndex("directory"));
						_path = newTable.getRow().getString(newTable
								.getColumnIndex("path"));
						// sending add message
						FileConnection file = (FileConnection)Connector.open(fileName, Connector.READ_WRITE);

						if (null != file) {
							logger.log(
									TAG,
									"Message Being Sent: "
											+ newTable.getRow().getString(newTable
													.getColumnIndex("name"))
											+ ","
											+ newTable.getRow().getString(newTable
													.getColumnIndex("path"))
											+ ","
											+ newTable.getRow().getLong(newTable
													.getColumnIndex("time"))
											+ ","
											+ newTable.getRow().getLong(newTable
													.getColumnIndex("size"))
											+ ","
											+ newTable.getRow().getString(newTable
													.getColumnIndex("md5")));

							flyFileMessage.setMessage(add, _path, newTable
									.getRow().getLong(newTable.getColumnIndex("time")),
									newTable.getRow().getString(newTable
											.getColumnIndex("md5")));

							logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
									+ flyFileMessage.getREST());
							logger.log(TAG, "ADDING FILE DATA");

							// send file message
							logger.log(TAG, "Adding File Message..."); // set
																		// CRC
							// resultREST =
							// myServer.contactServer(flyFileMessage.getREST(),
							// String.valueOf(myServer.getCrcValue(fileAsHex)),
							// fileAsHex);
							resultREST = server.contactServer(
									flyFileMessage.getREST(), file);

							if (resultREST.isError()) {
								logger.log(TAG,
										"Error Sending File Post Message");
							} else {
								fileLog.setSent(_path);
							}

							flyFileMessage.clearData();
						}
					} else {
						logger.log(TAG, "File is null");
					}
					notFoundTable.next();
				}

				newTable.next();
				cursorCount--;
			}
			notFoundTable.close();
			newTable.close();

			/**
			 * Checking for file renamed/ path
			 */
			logger.log(TAG, "renamed");
			// check for re-named/path files
			logger.log(TAG, "checking for re-named/path files...");
			Cursor notFoundTable1 = fileLog.getNotFoundFileTable();
			Cursor newTable1 = fileLog.getNewFileTable();
			newTable1.last();
			cursorCount = newTable1.getPosition();
			// logger.log(TAG, "count4="+cursorCount);
			flyFileMessage.clearData();

			// newTable loop
			newTable1.first();
			notFoundTable1.first();
			while (newTable1.getPosition() == -1 && 0 != cursorCount) { // notFoundTable
																			// loop
				while (notFoundTable1.getPosition() == -1) { // check for md5
																// match
					if (newTable1.getRow().getString(newTable1.getColumnIndex("md5"))
							.equals(notFoundTable1.getRow().getString(notFoundTable1
									.getColumnIndex("md5")))) {
						// add update message
						flyFileMessage.setMessage(update, newTable1
								.getRow().getString(newTable1.getColumnIndex("path")),
								notFoundTable1.getRow().getString(notFoundTable1
										.getColumnIndex("path")), newTable1
										.getRow().getString(newTable1
												.getColumnIndex("md5")));

						logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
								+ flyFileMessage.getREST());
						// logger.log(TAG, "ADDING FILE DATA");

						// send file message
						logger.log(TAG, "Adding File Message...");
						actLog.addMessage(flyFileMessage);// .getREST());
						// resultREST = myServer.contactServer(flyFileMessage);

						flyFileMessage.clearData();
					}
					notFoundTable1.next();
				}
				newTable1.next();
				cursorCount--;
			}
			notFoundTable1.close();
			newTable1.close();

			/**
			 * Checking for file deleted
			 */
			logger.log(TAG, "deleted");
			// check for deleted files
			logger.log(TAG, "checking for deleted files...");
			Cursor deleteTable = fileLog.getNotFoundFileTable();
			deleteTable.last();
			cursorCount = deleteTable.getPosition();
			// logger.log(TAG, "count5="+cursorCount);
			flyFileMessage.clearData();

			deleteTable.first();
			while (deleteTable.getPosition() == -1 && 0 != cursorCount)// fileTable
																			// loop
			{
				logger.log(TAG, "file has been deleted");
				// logger.log(TAG,
				// "Int key for delete"+deleteTable.getInt(deleteTable.getColumnIndex("_id")));

				fileLog.delete(deleteTable.getRow().getInteger(deleteTable .getColumnIndex("_id")));

				// add delete message
				flyFileMessage
						.setMessage(delete, deleteTable.getRow().getString(deleteTable
								.getColumnIndex("path")), 0, deleteTable
								.getRow().getString(deleteTable.getColumnIndex("md5")));

				logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
						+ flyFileMessage.getREST());
				// logger.log(TAG, "ADDING FILE DATA");
				// send file message
				logger.log(TAG, "Adding File Message...");
				actLog.addMessage(flyFileMessage);// .getREST());
				// resultREST = myServer.contactServer(flyFileMessage);

				flyFileMessage.clearData();

				deleteTable.next();
				cursorCount--;
			}
			deleteTable.close();

			/**
			 * Checking for newly file added
			 */
			logger.log(TAG, "added");
			// check for newly added files
			logger.log(TAG, "checking for newly added files...");
			Cursor addTable = fileLog.getNewFileTable();
			addTable.last();
			cursorCount = addTable.getPosition();
			// logger.log(TAG, "count6="+cursorCount);
			flyFileMessage.clearData();
			int count = 0;
			addTable.first();
			while (addTable.getPosition() == -1 && 0 != cursorCount)// fileTable
																		// loop
			{
				logger.log(TAG, "new file has been added");
				flyFileMessage.clearData();
				logger.log(TAG, "getting file");
				fileName = addTable.getRow().getString(addTable.getColumnIndex("name"));
				fileDir = addTable.getRow().getString(addTable
						.getColumnIndex("directory"));
				_path = addTable.getRow().getString(addTable.getColumnIndex("path"));
				logger.log(TAG, "looking for:" + fileName);
				// fileAsHex =
				// findFile(Environment.getExternalStorageDirectory(), fileName,
				// fileDir);
				FileConnection file = (FileConnection)Connector.open(fileName, Connector.READ_WRITE);
				// logger.log(TAG,
				// "Size of fileAsHex in endpass= "+fileAsHex.length());
				try {
					Thread.sleep(1000 * 1 * 10);
				} // pause to aid garbage collection
				catch (InterruptedException e) {
					actLog.addMessage(new ErrorMessage(e));
				}

				System.gc();// call for garbage to be collected
				// if(null != fileAsHex)//null != hex && 0 < hex.length())
				if (null != file) {
					flyFileMessage.setMessage(add, _path,
							addTable.getRow().getLong(addTable.getColumnIndex("time")),
							addTable.getRow().getString(addTable.getColumnIndex("md5")));

					logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
							+ flyFileMessage.getREST());
					resultREST = server.contactServer(flyFileMessage.getREST(),
							file);

					count++;
					logger.log(TAG, "Decrypted message (" + count + "): "
							+ resultREST.getREST());

					if (resultREST.isError()) {
						logger.log(TAG, "Error Sending File Post Message");
					} else {
						fileLog.setSent(_path);
					}

					flyFileMessage.clearData();
				} else {
					logger.log(TAG, "File is null");
				}
				addTable.next();
				cursorCount--;
			}
			addTable.close();

			/**
			 * Checking for Unsent files
			 */
			logger.log(TAG, "unsent");
			// check for Unsent files
			logger.log(TAG, "checking for Unsent files...");
			Cursor unsentTable = fileLog.getUnSentFileTable();
			unsentTable.last();
			cursorCount = unsentTable.getPosition();
			flyFileMessage.clearData();
			unsentTable.first();
			while (unsentTable.getPosition() == -1 && 0 != cursorCount)// fileTable
																			// loop
			{
				logger.log(TAG, "UnSent file has been added");
				flyFileMessage.clearData();
				logger.log(TAG, "getting file");
				fileName = unsentTable.getRow().getString(unsentTable
						.getColumnIndex("name"));
				fileDir = unsentTable.getRow().getString(unsentTable
						.getColumnIndex("directory"));
				_path = unsentTable.getRow().getString(unsentTable
						.getColumnIndex("path"));
				logger.log(TAG, "looking for:" + fileName);
				FileConnection file = (FileConnection)Connector.open(fileName, Connector.READ_WRITE);

				System.gc();// call for garbage to be collected
				if (null != file) {

					flyFileMessage.setMessage(add, _path, unsentTable
							.getRow().getLong(unsentTable.getColumnIndex("time")),
							unsentTable.getRow().getString(unsentTable
									.getColumnIndex("md5")));

					logger.log(TAG, "THE STRING BEING SENT TO SERVER: "
							+ flyFileMessage.getREST());
					resultREST = server.contactServer(flyFileMessage.getREST(),
							file);

					if (resultREST.isError()) {
						logger.log(TAG, "Error Sending File Post Message");
					} else {
						fileLog.setSent(_path);
					}

					flyFileMessage.clearData();
				} else {
					logger.log(TAG, "File is null");
				}
				unsentTable.next();
				cursorCount--;
			}
			unsentTable.close();
		}

		logger.log(TAG, "Finished End Pass");

		logger.log(TAG, "=======Database Output==========");
		Cursor dbOutput = fileLog.getAll();
		// int i = 1;
		dbOutput.first();
		while (dbOutput.getPosition() == -1) {
			logger.log(
					TAG,
					"Row "
							+ dbOutput.getRow().getString(dbOutput
									.getColumnIndex("sent"))
							+ " 	| "
							+ dbOutput.getRow().getString(dbOutput.getColumnIndex("_id"))
							+ " 	| "
							+ dbOutput.getRow().getString(dbOutput
									.getColumnIndex("name"))
							+ " 	| "
							+ dbOutput.getRow().getString(dbOutput
									.getColumnIndex("directory"))
							+ " 	| "
							+ dbOutput.getRow().getString(dbOutput
									.getColumnIndex("path"))
							+ " 	| "
							+ dbOutput.getRow().getLong(dbOutput.getColumnIndex("time"))
							+ " 	| "
							+ dbOutput.getRow().getLong(dbOutput.getColumnIndex("size"))
							+ " 	| "
							+ dbOutput.getRow().getString(dbOutput.getColumnIndex("md5"))
							+ " 	| ");

			logger.log(TAG, "=========");
			dbOutput.next();
		}
		dbOutput.close();
		logger.log(TAG, "==========");

	}

	/**
	 * This method passes the md5 created in MediaSync for a new file to fileLog
	 * to be stored
	 * 
	 * @param md5
	 *            - md5 file value
	 */
	public void setMd5(String md5) {
		fileLog.setMd5(md5);
	}

	public String[] findDuplicate(String inputPath) {
		String[] duplicates = null;
		try {
			logger.log(TAG, "finding duplicates");
			duplicates = fileLog.findDuplicate(inputPath);
		} catch (Exception e) {
			e.printStackTrace();
			actLog.addMessage(new ErrorMessage(e));
		}
		if (null == duplicates) {
			duplicates = new String[1];
			duplicates[0] = inputPath;
		}
		return duplicates;
	}

	class FileMessage implements Message {
		private final int type = 22;
		private String target;
		private String path;
		private String timeOROldPath;
		private String md5;
		private MMTools tools = ToolsBB.getInstance();

		/**
		 * The constructor initialises all the message parameters
		 */
		public FileMessage() {
			clearData();
		}

		/**
		 * This method adds the file information to the file message object.
		 * 
		 */
		public void setMessage(String _target, String _path, long _time,
				String _md5) {
			setMessage(_target, _path, tools.getDate(_time), _md5);
		}

		public void setMessage(String _target, String _path, String _oldPath,
				String _md5) {
			target = _target;
			path = _path;
			timeOROldPath = _oldPath;
			md5 = _md5;
		}

		/**
		 * This method removes the current data in the message and initialises
		 * the parameters.
		 * 
		 */
		public void clearData() {
			// This is used to ensure good practices and save
			// resources on the device.
			target = null;
			path = null;
			timeOROldPath = null;
			md5 = null;
		}

		public String getREST() {
			return Registration.getRegID() + Tools.ServerQueryStringSeparator
					+ type + Tools.ServerQueryStringSeparator + tools.getDate()
					+ Tools.ServerQueryStringSeparator + target
					+ Tools.ServerQueryStringSeparator + path
					+ Tools.ServerQueryStringSeparator + timeOROldPath
					+ Tools.ServerQueryStringSeparator + md5;
		}

		public String getTime() {
			return timeOROldPath;
		}

		public int getType() {
			return type;
		}
	}
}
