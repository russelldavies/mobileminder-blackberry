package com.kids.Data;

import java.io.InputStream;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.io.File;

import com.kids.Logger;
import com.kids.Registration;
import com.kids.net.ErrorMessage;
import com.kids.net.Reply;
import com.kids.net.Server;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

/**
 * This class analyses the device file system and relays any changes on to the local and non local file system storage
 */

public class FileInfoManager {
	
	private FileDataAccess fileLog;//This is the local database for file system information
	private LocalDataReader actLog;
	private LocalDataReader localData;
	//private Context context;
	//private MediaSyncLegacy mediaSync;
	private FileInfo fileInfo =  new FileInfo();
	private MMLinkedList notSentYet = new MMLinkedList();
	private FileMessage flyFileMessage = new FileMessage();
	private Server myServer;
    private static final String  add		  = "ADD";
    private static final String  update		  = "UPD";
    private static final String  delete		  = "DEL";
    private boolean dbEmpty;
    private Debug logger = Logger.getInstance();
    private boolean preformMd5;
    private boolean nameMatch;
    private boolean pathMatch;
    private boolean sizeMatch;
    private String filterName;
    //private String fileAsHex = "";
    
    //private static final char[] kDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };


/**
 * This is the constructor for the class, it creates: 
 * 1) a local storage for file information 
 * 2) a local storage for file messages to be stored
 * 3) an instance of MediaSync to avail of it's methods
 * 4) an instance of server
 * 
 * @param _context Interface to global environment that the current application is running in.
 * @param _localData local message storage 
 */
	public FileInfoManager(LocalDataReader _localData) 
	{
    	fileLog  = new FileDataAccess();
    	localData = _localData;
    	//mediaSync =  new MediaSyncLegacy(_context);
    	myServer = new Server(actLog);
	}

/**
 * This method prepares the db table for the the new file system changes to be added
 */
	public void startPass()
	{	
		if(!fileLog.isOpen())
		{	fileLog  = new FileDataAccess();	}
		
		String ValueAsString = new Boolean(fileLog.isEmpty()).toString();
		logger.log("StartPass: DB is empty? = "+ValueAsString);
		if(0 != fileLog.length())
		{	logger.log("DB is NOT empty");
			logger.log("cleaning...");
			//sets the new and found values for each row to false
			fileLog.clean();
			dbEmpty = false;
		}
		else{	dbEmpty = true; }
	}//end startpass
	
/**
 * This method sorts the files found in the system, passing new file information to fileLog to be recorded.
 * This is short hand version of addFile, designed to take a file as a paramater.
 * 
 * @param _name file name
 * @param _path	file path
 * @param _lastMod last modified time
 * @param _size	file size
 * @return	true if the file is new
 */
	public boolean addFile(File _file)
	{	
		return addFile(_file.getName(), _file.getParent(), _file.getAbsolutePath(), _file.lastModified(), _file.length());
	}
	
/**
 * This method sorts a files found in the device file system, passing the new file information to fileLog to be recorded
 * 
 * @param _name file name
 * @param _path	file path
 * @param _lastMod last modified time
 * @param _size	file size
 * @return	true if the file is new
 */
	public boolean addFile(String _name, String _directory, String _path, long _lastMod, long _size)
	{
		//fileInfo(boolean _newFile, boolean _found, String _name, String _path, long _lastModifiedTime, int _size, String _md5)
		preformMd5 = false;
		
		//String theValueAsString = new Boolean(fileLog.isEmpty()).toString();
		//logger.log("AddFile DB is empty? = "+theValueAsString);
		if(dbEmpty)//initial files added
		{	//logger.log("fileLog is empty");
		
			fileInfo.setNewFile(true);
			fileInfo.setFound(true);
			fileInfo.setSent(false);
			fileInfo.setName(_name);						
			fileInfo.setDirectory(_directory);																							
			fileInfo.setPath(_path);																		
			fileInfo.setLastModifiedTime(_lastMod);
			fileInfo.setSize(_size);
			
			//logger.log("Adding file: "+_name+","+_path+","+_lastMod+","+_size);
			//add the file to the file-table
			fileLog.add(fileInfo);
			//set to true if the file need an MD5 value
			preformMd5 = fileInfo.isNewFile();
			//clean fileInfo fly object
			fileInfo.clearData();
		}
		else
		{
			//cursor to a table consisting of name, path and size data for all the files in the database
			Cursor processLog = fileLog.fileCheckTable();
			logger.log("checking fileLog");
			nameMatch = false;
			pathMatch = false;
			sizeMatch = false;
			
			processLog.moveToFirst();
	        while (processLog.isAfterLast() == false)//file check
	        {
	        	if(_name.equals(processLog.getString(processLog.getColumnIndex("name"))))
				{ nameMatch = true; }
	        	if(_path.equals(processLog.getString(processLog.getColumnIndex("path"))))
				{ pathMatch = true; }
				if(_size == (processLog.getLong(processLog.getColumnIndex("size"))))
				{ sizeMatch = true; }
	        
	            processLog.moveToNext();
	        }
	        processLog.close();
					
	        if(nameMatch && pathMatch && sizeMatch == true)//file[name, path or size] have NOT changed
			{
	        	logger.log("MATCHED");
				preformMd5 = false;
				//clean fileInfo fly-weight object
				fileInfo.clearData();
				//logger.log("File: "+_name+", matched in DB");
				fileLog.setFound(_name);
				
				if(!fileLog.isSent(_path))
				{	
					logger.log("NOT SENT");
					notSentYet.add(_path);	
				}
				
				logger.log("Matched: "+_name+"namematched?: "+nameMatch);
			}
			else
			{
				logger.log("No Matched: "+_name);
				fileInfo.setNewFile(true);
				fileInfo.setFound(true);
				fileInfo.setSent(false);
				fileInfo.setName(_name);
				fileInfo.setDirectory(_directory);
				fileInfo.setPath(_path);
				fileInfo.setLastModifiedTime(_lastMod);
				fileInfo.setSize(_size);
				
				//add the file to the file-table
				fileLog.add(fileInfo);
				//set to true if the file need an MD5 value
				preformMd5 = fileInfo.isNewFile();
				//clean fileInfo fly object
				fileInfo.clearData();
				//logger.log("File: "+_name+", NOT matched in DB...Adding");
			}
		}
		return preformMd5;
	}//end addfile
	
/**
 * This method sorts and updates the local database of file system information matching it to the device file system.
 * This new file system information & structure is then relayed on to the server.
 */
	public void endPass()
	{
		//check for initial sync
		Reply resultREST;
		String fileName;
		String fileDir;
		String _path;
		int cursorCount;
		Cursor fileTable = fileLog.getPrivateStoreDBoutput();
		boolean initialSync = true;
		cursorCount = fileTable.getCount();
        //logger.log("count1="+cursorCount);
		
		fileTable.moveToFirst();
        while (fileTable.isAfterLast() == false && 0 != cursorCount )//fileTable loop
        {
        	if(0 == (fileTable.getInt(fileTable.getColumnIndex("new"))));
        	{ initialSync = false; }
        	fileTable.moveToNext();
			cursorCount--;
        }
        
        if ( fileTable != null ) 
        {	fileTable.close(); } 
        
		if(initialSync)
		{	logger.log("Initial Sync Sending Files...");
		
			Cursor fileTable2 = fileLog.getPrivateStoreDBoutput();
			cursorCount = fileTable2.getCount();
	        //logger.log("count2="+cursorCount);
			
			fileTable2.moveToFirst();
			while (fileTable2.isAfterLast() == false && 0 != cursorCount )//fileTable loop
	        {
				fileName = fileTable2.getString(fileTable2.getColumnIndex("name"));
				fileDir = fileTable2.getString(fileTable2.getColumnIndex("directory"));
				_path = fileTable2.getString(fileTable2.getColumnIndex("path"));
				File file = findFile(Environment.getExternalStorageDirectory(), fileName, fileDir);
				
	        	try 
	        	{	Thread.sleep(1000*1*10);	} //pause to aid garbage collection
	        	catch (InterruptedException e) 
				{	actLog.addMessage(new ErrorMessage(e));	}
				
	        	if(null != file)
	        	{
		        	//logger.log("ADDING FILE DATA");
			        flyFileMessage.setMessage(add,
			        																		    _path,
							        			fileTable2.getLong(fileTable2.getColumnIndex("time")),
							        			fileTable2.getString(fileTable2.getColumnIndex("md5")));
			        
			        //logger.log("CRC="+String.valueOf(myServer.getCrcValue(hex))+" PHOTOHEX="+ hex);
		        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
		        	//send file message
			        logger.log("Adding File Message...");											//set CRC
					//resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(fileAsHex)), fileAsHex);
			        resultREST = myServer.contactServer(flyFileMessage.getREST(), file);
			        
					
					if(resultREST.isError())
					{	 logger.log("Error Sending File Post Message");	}
					else
					{	fileLog.setSent(_path);	}
					
					flyFileMessage.clearData();
	        	}
	        	else
	        	{	logger.log("File is null");}
	        	
				fileTable2.moveToNext();
				cursorCount--;
	        }
		       if ( fileTable2 != null ) 
		        {fileTable2.close();} 

		}
		else
		{
			/**
			 * Checking for unsent files
			 */
			logger.log("NotSentYetAraylist Size: "+notSentYet.size());
		    for (MMLinkedListIterator theItr = notSentYet.getIterator(); theItr.isValid(); theItr.advance())
		    {	
		    	_path = (String) theItr.retrieve();
				logger.log(_path+": UNSENT-sending now...");
		    	Cursor unsentFiles = fileLog.getFileInfo(_path);
		    	unsentFiles.moveToFirst();
		    	//do here
		    	fileName = unsentFiles.getString(unsentFiles.getColumnIndex("name"));
				fileDir = unsentFiles.getString(unsentFiles.getColumnIndex("directory"));
				File file = findFile(Environment.getExternalStorageDirectory(), fileName, fileDir);
				
	        	try 
	        	{	Thread.sleep(1000*1*10);	} //pause to aid garbage collection
	        	catch (InterruptedException e) 
				{	actLog.addMessage(new ErrorMessage(e));	}
				
	        	//logger.log("ADDING FILE DATA");
		        flyFileMessage.setMessage(add,
		        		unsentFiles.getString(unsentFiles.getColumnIndex("path")),
		        		unsentFiles.getLong(unsentFiles.getColumnIndex("time")),
		        		unsentFiles.getString(unsentFiles.getColumnIndex("md5")));
		        
		        //logger.log("CRC="+String.valueOf(myServer.getCrcValue(hex))+" PHOTOHEX="+ hex);
	        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
	        	//send file message
		        logger.log("Adding File Message...");											//set CRC
				//resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(fileAsHex)), fileAsHex);
		        resultREST = myServer.contactServer(flyFileMessage.getREST(), file);
		    
				
				if(resultREST.isError() )
				{	 logger.log("Error Sending File Post Message");	}
				else
				{	fileLog.setSent(_path);	}
				
				flyFileMessage.clearData();
	        	
				unsentFiles.close();
		    }
		    notSentYet.makeEmpty();
			
			/**
			 * Checking for file edited
			 */
			logger.log("fileedit");
				 //check if file was edited
				logger.log("checking if file was edited...");
				Cursor notFoundTable = fileLog.getNotFoundFileTable();
				Cursor newTable = fileLog.getNewFileTable();
				cursorCount = newTable.getCount();
		        //logger.log("count3="+cursorCount);
		        flyFileMessage.clearData();
				//newTable loop
				newTable.moveToFirst();
				notFoundTable.moveToFirst();
		        while (newTable.isAfterLast() == false && 0 != cursorCount )
		        {
			        	//notFoundTable loop
			            while (notFoundTable.isAfterLast() == false)
			            {	//logger.log("Name for compare"+newTable.getString(newTable.getColumnIndex("name")));
			            	
			            	//check for name and path match
			            	if(newTable.getString(newTable.getColumnIndex("name")).equals
			            			(notFoundTable.getString(notFoundTable.getColumnIndex("name"))) 
				            			&& newTable.getString(newTable.getColumnIndex("path")).equals
				            				(notFoundTable.getString(notFoundTable.getColumnIndex("path"))))
			            	{
			            		if(!newTable.getString(newTable.getColumnIndex("md5")).equals
				            			(notFoundTable.getString(notFoundTable.getColumnIndex("md5")))) 
				            	{
			            			logger.log("file has been edited");
			            			fileLog.updateMd5(notFoundTable.getInt(notFoundTable.getColumnIndex("_id")),
			            							  newTable.getString(newTable.getColumnIndex("md5")), 
			            							  newTable.getLong(newTable.getColumnIndex("time")),
			            							  newTable.getLong(newTable.getColumnIndex("size")));
			            			
				            		int rows = fileLog.delete(newTable.getInt
				            				(newTable.getColumnIndex("_id")));
				            		 logger.log("File deleted, rows effected:"+rows);
				            	}																
										
					            //add delete message
					    	    flyFileMessage.setMessage(delete,
					    	    							newTable.getString(newTable.getColumnIndex("path")),
								    	        			0,
							    							newTable.getString(newTable.getColumnIndex("md5")));
					    	    
					    	    logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
					    	    //logger.log("ADDING FILE DATA");
								        
								//send file message
								logger.log("Adding File Message...");		
								localData.addMessage(flyFileMessage);//.getREST());
								//resultREST = myServer.contactServer(flyFileMessage);
										
								flyFileMessage.clearData();
								
								fileName = newTable.getString(newTable.getColumnIndex("name"));
								fileDir = newTable.getString(newTable.getColumnIndex("directory"));
								_path = newTable.getString(newTable.getColumnIndex("path"));
								//sending add message
								File file = findFile(Environment.getExternalStorageDirectory(), fileName, fileDir);
					        	
					        	try 
					        	{	Thread.sleep(1000*1*10);	} //pause to aid garbage collection
					        	catch (InterruptedException e) 
								{	actLog.addMessage(new ErrorMessage(e));	}
					        	
					        	if(null != file)
					        	{
						        	logger.log("Message Being Sent: "+
	        								newTable.getString(newTable.getColumnIndex("name"))+","+
	        								newTable.getString(newTable.getColumnIndex("path"))+","+
	        								newTable.getLong(newTable.getColumnIndex("time"))+","+
	        								newTable.getLong(newTable.getColumnIndex("size"))+","+
	        								newTable.getString(newTable.getColumnIndex("md5")));	
						        	
						        	flyFileMessage.setMessage(add,
						        																			_path,
						        								newTable.getLong(newTable.getColumnIndex("time")),
						        								newTable.getString(newTable.getColumnIndex("md5")));
						        	
						        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
						        	logger.log("ADDING FILE DATA");
							        
									//send file message
							        logger.log("Adding File Message...");											//set CRC
									//resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(fileAsHex)), fileAsHex);
							        resultREST = myServer.contactServer(flyFileMessage.getREST(), file);
									
									if(resultREST.isError())
									{	 logger.log("Error Sending File Post Message");	}
									else
									{	fileLog.setSent(_path);	}
									
									flyFileMessage.clearData();
					        	}
			            	}
				        	else
				        	{	logger.log("File is null");}
			            		notFoundTable.moveToNext();
			            }
		
		        	newTable.moveToNext();
		        	cursorCount--;
		        }
		        notFoundTable.close();
		        newTable.close();

				
				/**
				 * Checking for file renamed/ path
				 */
		        logger.log("renamed");
		        //check for re-named/path files
		        logger.log("checking for re-named/path files...");
				Cursor notFoundTable1 = fileLog.getNotFoundFileTable();
				Cursor newTable1 = fileLog.getNewFileTable();
				cursorCount = newTable1.getCount();
		        //logger.log("count4="+cursorCount);
		        flyFileMessage.clearData();
				 
				//newTable loop
				newTable1.moveToFirst();
				notFoundTable1.moveToFirst();
		        while (newTable1.isAfterLast() == false && 0 != cursorCount )
		        {		//notFoundTable loop
			            while (notFoundTable1.isAfterLast() == false)
			            {	//check for md5 match
			            	if(newTable1.getString(newTable1.getColumnIndex("md5")).equals
			            			(notFoundTable1.getString(notFoundTable1.getColumnIndex("md5"))))
			            	{
			            		//add update message
			    	        	flyFileMessage.setMessage(update,
			    	        			newTable1.getString(newTable1.getColumnIndex("path")),
			    	        			notFoundTable1.getString(notFoundTable1.getColumnIndex("path")),
		    							newTable1.getString(newTable1.getColumnIndex("md5")));
			    	        	
			    	        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
			    	        	//logger.log("ADDING FILE DATA");
								        
								//send file message
								logger.log("Adding File Message...");		
								localData.addMessage(flyFileMessage);//.getREST());
								//resultREST = myServer.contactServer(flyFileMessage);
										
								flyFileMessage.clearData();
			            	}
			            		notFoundTable1.moveToNext();
			            }
			        newTable1.moveToNext();
		        	cursorCount--;
		        }
		        notFoundTable1.close();
		        newTable1.close();
		        
		        /**
				 * Checking for file deleted
				 */
		        logger.log("deleted");
		        //check for deleted files
		        logger.log("checking for deleted files...");
		        Cursor deleteTable = fileLog.getNotFoundFileTable();
		        cursorCount = deleteTable.getCount();
		        //logger.log("count5="+cursorCount);
		        flyFileMessage.clearData();
		        
		        deleteTable.moveToFirst();
		        while (deleteTable.isAfterLast() == false && 0 != cursorCount )//fileTable loop
		        {	logger.log("file has been deleted");
		        	//logger.log("Int key for delete"+deleteTable.getInt(deleteTable.getColumnIndex("_id")));
		        	
		    		int rows = fileLog.delete(deleteTable.getInt(deleteTable.getColumnIndex("_id")));
		    		logger.log("File deleted, rows effected:"+rows);
		    		
		            //add delete message
		    	    flyFileMessage.setMessage(delete,
		    	    							deleteTable.getString(deleteTable.getColumnIndex("path")),
					    	        			0,
				    							deleteTable.getString(deleteTable.getColumnIndex("md5")));
		    	    
		    	    logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
		    	    //logger.log("ADDING FILE DATA");
		    	    //send file message
			        logger.log("Adding File Message...");	
			        localData.addMessage(flyFileMessage);//.getREST());
					//resultREST = myServer.contactServer(flyFileMessage);
					
					flyFileMessage.clearData();
		        	
		        	deleteTable.moveToNext();
		        	cursorCount--;
		        }
		        deleteTable.close();
		
				/**
				 * Checking for newly file added
				 */
		        logger.log("added");
		        //check for newly added files
		        logger.log("checking for newly added files...");
		        Cursor addTable = fileLog.getNewFileTable();
		        cursorCount = addTable.getCount();
		       //logger.log("count6="+cursorCount);
		        flyFileMessage.clearData();
		        int count =0;
		        addTable.moveToFirst();
		        while (addTable.isAfterLast() == false && 0 != cursorCount)//fileTable loop
		        {
		        	logger.log("new file has been added");
					flyFileMessage.clearData();
					logger.log("getting file");			
					fileName = addTable.getString(addTable.getColumnIndex("name"));
					fileDir = addTable.getString(addTable.getColumnIndex("directory"));
					_path = addTable.getString(addTable.getColumnIndex("path"));
					logger.log("looking for:"+fileName);
					//fileAsHex = findFile(Environment.getExternalStorageDirectory(), fileName, fileDir);
					File file = findFile(Environment.getExternalStorageDirectory(), fileName, fileDir);
					//logger.log("Size of fileAsHex in endpass= "+fileAsHex.length());
		        	try 
		        	{	Thread.sleep(1000*1*10);	} //pause to aid garbage collection
		        	catch (InterruptedException e) 
					{	actLog.addMessage(new ErrorMessage(e));	}
					
					System.gc();//call for garbage to be collected
					//if(null != fileAsHex)//null != hex && 0 < hex.length())
					if(null != file)
		        	{
			        	//logger.log("Message Being Sent: "+addTable.getString(addTable.getColumnIndex("name")));
		        		/*+
			        			addTable.getString(addTable.getColumnIndex("name"))+","+
								addTable.getString(addTable.getColumnIndex("path"))+","+
								addTable.getLong(addTable.getColumnIndex("time"))+","+
								addTable.getLong(addTable.getColumnIndex("size"))+","+
								addTable.getString(addTable.getColumnIndex("md5")));*/
			        	
			        	flyFileMessage.setMessage(add,
			        																			_path,
								        			addTable.getLong(addTable.getColumnIndex("time")),
								        			addTable.getString(addTable.getColumnIndex("md5")));
			        	
			        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
			        	//logger.log("ADDING FILE DATA");
				        
			        	//Log.v("ClearViewKids", "CVK :: Message Being Sent: "+flyFileMessage.getREST());
			        	
						//send file message
				        //logger.log("Adding File Message...CRC="+String.valueOf(myServer.getCrcValue(fileAsHex)));											//set CRC
						//resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(fileAsHex)), fileAsHex);
				        resultREST = myServer.contactServer(flyFileMessage.getREST(), file);
						
						count++;
						Log.v("ClearViewKids", "CVK :: Decrypted	Message("+count+") From Server: "+resultREST.getREST());
						
						if(resultREST.isError())
						{	 logger.log("Error Sending File Post Message");	}
						else
						{	fileLog.setSent(_path);	}
						
						flyFileMessage.clearData();
		        	}
		        	else
		        	{	logger.log("File is null");}
		    		addTable.moveToNext();
		    		cursorCount--;
		        }
		        addTable.close();
		}
		
		Log.v("ClearViewKids", "CVK :: Finished Endpass");
		
		logger.log("Finished End Pass");
		
		logger.log("=============================================Database Output========================================================");
		Cursor dbOutput = fileLog.getPrivateStoreDBoutput();
		int i = 1;
		dbOutput.moveToFirst();
        while (dbOutput.isAfterLast() == false)//fileTable loop
        {
        		logger.log("Row "+
        		dbOutput.getString(dbOutput.getColumnIndex("sent"))+" 	| "+
        		dbOutput.getString(dbOutput.getColumnIndex("_id"))+" 	| "+
    			dbOutput.getString(dbOutput.getColumnIndex("name"))+" 	| "+
    			dbOutput.getString(dbOutput.getColumnIndex("directory"))+" 	| "+
				dbOutput.getString(dbOutput.getColumnIndex("path"))+" 	| "+
				dbOutput.getLong(dbOutput.getColumnIndex("time"))+" 	| "+
				dbOutput.getLong(dbOutput.getColumnIndex("size"))+" 	| "+
				dbOutput.getString(dbOutput.getColumnIndex("md5"))+" 	| ");
        	
        logger.log("===================================================================================================================");
        dbOutput.moveToNext();
        	i++;
        }
        dbOutput.close();
        logger.log("===================================================================================================================");
        fileLog.closeDB();
	}//end endpass
	
	
	public File findFile(File fileSystem, String name, String directory)
	{	
		String filePath = directory+"/"+name;
		File file = new File(filePath);
		if(file.exists())
		{	return file;	}
		else
		{	return null;	}
	}
	
	public String findFile1(File fileSystem, String name, String directory)
	{	
		filterName = name;
		File fileDir = new File(directory);
		//FileFilter fileFilter = new FileFilter(name);
		FileFilter fileFilter = new FileFilter();
		File[] files = fileDir.listFiles(fileFilter);
		fileDir = null;
		String hexVal = null;
		logger.log("Number of Files Found= "+files.length);
		
		if(isSdPresent() && null != files)//check for sdcard
		{
			hexVal = getHexFileString(files[0]);logger.log("FOUND1");
			files = null;
			fileFilter = null;
			fileDir = null;

		}
		else
		{logger.log("isSdPresent() = "+isSdPresent()+" OR this file is "+files);}
		//logger.log("HexVal Size="+hexVal.length());
		logger.log("Size of HexVal in FF= "+hexVal.length());
		
		int len = hexVal.length();
		int count = 0, num = 100;
		while(count < len)
		{
			if (count == num)
			{
				Controller.log("Media Photo Hex:"+hexVal.substring((count - 100), count)+":");
				num += 100;
			}
			count++;
		}
		
        return hexVal;//not found :(
	}
	
	
	//new methods from mediasync
	/*
	/**
	 * This method digs through the file system and retrieves a HEX string of a file, searching by it's name.
	 * @param f URI to the file system
	 * @param file directory
	 * @return the file formated to a HEX string
	 
		public boolean findFile(File fileSystem, String name, String directory)
		{	
			FileFilter fileFilter = new FileFilter(name);
			fileFilter.accept(new File(directory), name);
			
			File[] files = fileSystem.listFiles(fileFilter);
			boolean hexVal = false;
			
			if(isSdPresent() && null != files)//check for sdcard
			{
		        for (int i = 0; i < files.length && false == hexVal; i++) 
		        {
		        	if (null != directory[0]              
		        	   && files[i].isDirectory()
		        	   && files[i].getName().equals(directory[0]))
		        	{
		        		logger.log("Go into:"+directory[0]);
		        		hexVal = findFile(files[i], name, regresArray(directory));
		        	}
	                else if (files[i].isFile() 
	                		&& files[i].getName().equals(name))
	                {	hexVal = getHexFileString(files[i]);logger.log("FOUND1");}
		        	
		        }
			}
			else
			{logger.log("isSdPresent() = "+isSdPresent()+" OR this file is "+files);}
			//logger.log("HexVal Size="+hexVal.length());
	        return hexVal;//not found :(
		}
		
		
		private String[] regresArray(String[] elements)
		{	return regresArray(elements, 1);}
		
		private String[] regresArray(String[] elements, int offset)
		{
			if(0 > offset || offset >= elements.length)
			{throw new IndexOutOfBoundsException();}
			
			int count = 0;
			while(count < elements.length)//for(int count = 0;  count++)
			{
				if(count+offset< elements.length)
				{	elements[count] = elements[count+offset];}
				else
				{	elements[count] = null;	}
				count++;
			}
			return elements;
		}
		
		*/

	/**
	 * This method formats a file byte array into a hex string
	 * 
	 * @param _file input file
	 * @return hex string of file
	 */
		private String getHexFileString(File _file)
		{
			//change to manage memory with:
	        //byte[] byteStream = new byte[1024];//[(int) _file.length()];
			InputStream fis = null;		
			int SIZE = (int) (_file.length() * 2);
	        byte[] byteStream = new byte[SIZE];
	        //boolean fileHexString = false;
	        String fileHexString = null;
	        try
	        {
	        	fis = new FileInputStream(_file);
	        	fis = new HexFilter(fis);
	        	fis.read(byteStream, 0, SIZE);

		        //fis.close();
		        //fis = null;

	        	logger.log("Size of byteStream= "+byteStream.length);
	        	
		        	//fileHexString = byteArrayToHexString(byteStream);
		        	fileHexString = new String(byteStream);
		        	byteStream = null;
			}
			catch (FileNotFoundException e1) 
			{	actLog.addMessage(new ErrorMessage(e1));	}
	        catch (IOException e2) 
	        {	actLog.addMessage(new ErrorMessage(e2));	}
		    catch(OutOfMemoryError e3)
		    {	actLog.addMessage(new ErrorMessage(e3));	}
		    finally 
		    {	
		    	try 
		    	{	if (fis != null) 
			    	{	fis.close();	}	
		    	} 
		    	catch (IOException e) 
		    	{	actLog.addMessage(new ErrorMessage(e));	}	
		    }
		    
		    //logger.log("HEX of file from device="+fileHexString.length);
			return fileHexString;
		}
		
	/**
	 * This method formats a byte-array into a hex string
	 * 
	 * @param b byte-array
	 * @return hex string
	 
		 public boolean byteArrayToHexString(byte[] b) 
		 {
		    char[] hexVal = new char[b.length * 2];
		    int value = 0;
		    for (int i = 0; i < b.length; i++) 
		    {
		         value = (b[i] + 256) % 256;
			     hexVal[i * 2 + 0] = kDigits[value >> 4];
			     hexVal[i * 2 + 1] = kDigits[value & 0x0f];
		    }
		    if(0 != hexVal.length)
		    {
		    	fileAsHex = new String(hexVal);
		    	hexVal = null;
		    	return true;
		    }
		    else
		    {	return false;	}
		    
	    	
		 } 
		 
		 */
		 /**
		  * This method determines if their is a mounted SD Card in the device
		  * @return true if their is a mounted SD Card
		  */
		 	public static boolean isSdPresent() 
		 	{
		 		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		 	}
		 
	//new methods from mediasync
	
	
/**
 * This method passes the md5 created in MediaSync for a new file to fileLog to be stored
 * 
 * @param _md5 md5 file value
 */
	public void setMd5(String _md5)
	{	//logger.log("Setting MD5");
		fileLog.setMd5(_md5);
	}
	
	public String[] findDuplicate(String _path)
	{	
		logger.log("finding duplicates");
		return fileLog.findDuplicate(_path);
	}
	

class FileFilter implements FilenameFilter 
{
	/*private String intentedFile;
	
	public FileFilter(String name)
	{
		intentedFile = name;
	}*/
	
	 public boolean accept(File directory, String _name) 
	 {
	   if (_name.equalsIgnoreCase(filterName)) return true;
	   return false;
	 }
}

	
	
/**
 * This class implements the message interface to hold file messages.
 */
	class FileMessage implements Message
	{
		private final int  type = 22;
		private String	target;
		private String	path;
		private String timeOROldPath;
		private String	md5;
		private StringBuffer stringREST;
		private MMTools tools = Tools.getInstance();
		
/**
 * The constructor initialises all the message parameters
 */
		public FileMessage()
		{clearData();}
		
		
/**
 * This method adds the file information to the file message object.
 * 
 */
		public void setMessage(String _target, String _path, long _time, String _md5)
		{setMessage( _target, _path, tools.getDate(_time),  _md5);}
		
		public void setMessage(String _target, String _path, String _oldPath, String _md5)
		{	
			target = _target;
			path = _path;
			timeOROldPath = _oldPath;
			md5 = _md5;
		}
		
/**
 * This method removes the current data in the message and initialises the parameters.
 * 
 */
		public void clearData()//This is used to ensure good practices and save resources on the device.
		{
			target = null;
			path = null;
			timeOROldPath = null;
			md5 = null;
			stringREST  = null;
		}

/**
* This method retrieves the message formatted in to a single string value.
*/
		//@Override
		public String getREST() 
		{
			
			if(null == stringREST)
			{	
				stringREST = new StringBuffer();
				stringREST.append(Registration.getRegID());
				stringREST.append(Tools.RestElementSeparator);
				stringREST.append(type);
				stringREST.append(Tools.RestElementSeparator);
				stringREST.append(tools.getDate());
				stringREST.append(Tools.RestElementSeparator);
				stringREST.append(target);
				stringREST.append(Tools.RestElementSeparator);
				stringREST.append(path);
				stringREST.append(Tools.RestElementSeparator);
				stringREST.append(timeOROldPath);
				stringREST.append(Tools.RestElementSeparator);
				stringREST.append(md5);
			}		

			return 	stringREST.toString();			

		}


		
/**
 * This method retrieves the time that is set on the device.
 * 
 * @return the device time
 */
		//@Override
		public String getTime() 
		{	return timeOROldPath;	}
		
/**
 * This method retrieves the type number for the file message
 * 
 * @return the type number corresponding to a file message
 */
		//@Override 
		public int getType() 
		{	return type;	}

	}

}