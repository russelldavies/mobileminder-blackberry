package com.spotlight.track;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.io.File;
import net.rim.device.api.io.FileInfo;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;

/*
import com.kids.net.Reply;
import com.kids.net.Server;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;

import net.rim.device.api.io.File;
import net.rim.device.api.io.FileInfo;

import com.kids.prototypes.Message;
import com.kids.Data.Tools;

*/

/**
 * This class analyses the device file system and relays any changes on to the local and non local file system storage
 */

public class FileInfoManager {
	
	private FileDataAccess fileLog;//This is the local database for file system information
	private LocalDataAccess actLog;
	private LocalDataWriter localData;
	private MediaSync mediaSync;
	private FileInfo fileInfo =  new FileInfo();
	private FileMessage flyFileMessage = new FileMessage();
	private Server myServer;
    private static final String  add		  = "ADD";
    private static final String  update		  = "UPD";
    private static final String  delete		  = "DEL";
    private boolean dbEmpty;
    private Debug logger = Logger.getInstance();


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
	public FileInfoManager(/*Context _context,*/ LocalDataWriter _localData) 
	{
    	fileLog  = new FileDataAccess();//(_context);
    	localData = _localData;
    	mediaSync =  new MediaSync();//(_context);
    	myServer = new Server(actLog);
	}

/**
 * This method prepares the db table for the the new file system changes to be added
 */
	public void startPass()
	{	
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
		return addFile(_file.getName(), _file.getAbsolutePath(), _file.lastModified(), _file.length());
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
	public boolean addFile(String _name, String _path, long _lastMod, long _size)
	{
		//fileInfo(boolean _newFile, boolean _found, String _name, String _path, long _lastModifiedTime, int _size, String _md5)
		boolean preformMd5 = false;
		String theValueAsString = new Boolean(fileLog.isEmpty()).toString();
		logger.log("AddFile DB is empty? = "+theValueAsString);
		if(dbEmpty)//initial files added
		{	//logger.log("fileLog is empty");
		
			fileInfo.setNewFile(true);
			fileInfo.setFound(true);
			fileInfo.setName(_name);
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
			boolean nameMatch = false;
			boolean pathMatch = false;
			boolean sizeMatch = false;
			
			processLog.first();//.moveToFirst();
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
					
	        if(nameMatch || pathMatch || sizeMatch != false)//file[name, path or size] have NOT changed
			{
				preformMd5 = false;
				//clean fileInfo fly object
				fileInfo.clearData();
				//logger.log("File: "+_name+", matched in DB");
				fileLog.setFound(_name);
			}
			else
			{

				fileInfo.setNewFile(true);
				fileInfo.setFound(true);
				fileInfo.setName(_name);
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
        
		if(initialSync != false)
		{	logger.log("Initial Sync Sending Files...");
		
			Cursor fileTable2 = fileLog.getPrivateStoreDBoutput();
			cursorCount = fileTable2.getCount();
	        //logger.log("count2="+cursorCount);
			
			fileTable2.moveToFirst();
			while (fileTable2.isAfterLast() == false && 0 != cursorCount )//fileTable loop
	        {
				String hex = mediaSync.findFile(Environment.getExternalStorageDirectory(), fileTable2.getString(fileTable2.getColumnIndex("name")));
	        	//logger.log("ADDING FILE DATA");
		        flyFileMessage.setMessage(add,
						        			fileTable2.getString(fileTable2.getColumnIndex("path")),
						        			fileTable2.getLong(fileTable2.getColumnIndex("time")),
						        			fileTable2.getString(fileTable2.getColumnIndex("md5")));
		        
		        logger.log("CRC="+String.valueOf(myServer.getCrcValue(hex))+" PHOTOHEX="+ hex);
	        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
	        	//send file message
		        logger.log("Adding File Message...");											//set CRC
				resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(hex)), hex);

				if(resultREST.isError() == false)
				{	 logger.log("Error Sending File Post Message");	}
				
				flyFileMessage.clearData();
	        	
				fileTable2.moveToNext();
				cursorCount--;
	        }
		       if ( fileTable2 != null ) 
		        {fileTable2.close();} 

		}
		else
		{
			/**
			 * Checking for file edited
			 */
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
				            		 //logger.log("File deleted, rows effected:"+rows);
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
										
								//sending add message
					        	String hex = mediaSync.findFile(Environment.getExternalStorageDirectory(), newTable.getString(newTable.getColumnIndex("name")));
					        	
					        	logger.log("Message Being Sent: "+
        								newTable.getString(newTable.getColumnIndex("name"))+","+
        								newTable.getString(newTable.getColumnIndex("path"))+","+
        								newTable.getLong(newTable.getColumnIndex("time"))+","+
        								newTable.getLong(newTable.getColumnIndex("size"))+","+
        								newTable.getString(newTable.getColumnIndex("md5")));	
					        	
					        	flyFileMessage.setMessage(add,
					        								newTable.getString(newTable.getColumnIndex("path")),
					        								newTable.getLong(newTable.getColumnIndex("time")),
					        								newTable.getString(newTable.getColumnIndex("md5")));
					        	
					        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
					        	logger.log("ADDING FILE DATA");
						        
								//send file message
						        logger.log("Adding File Message...");											//set CRC
								resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(hex)), hex);
								
								if(resultREST.isError() == false)
								{	 logger.log("Error Sending File Post Message");	}
								
								flyFileMessage.clearData();
			            	}
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
		    		//logger.log("File deleted, rows effected:"+rows);
		    		
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
		        //check for newly added files
		        logger.log("checking for newly added files...");
		        Cursor addTable = fileLog.getNewFileTable();
		        cursorCount = addTable.getCount();
		       //logger.log("count6="+cursorCount);
		        flyFileMessage.clearData();
		        
		        addTable.moveToFirst();
		        while (addTable.isAfterLast() == false && 0 != cursorCount)//fileTable loop
		        {
		        	logger.log("new file has been added");
					flyFileMessage.clearData();
					String hex = mediaSync.findFile(Environment.getExternalStorageDirectory(), addTable.getString(addTable.getColumnIndex("name")));
		        	
		        	logger.log("Message Being Sent: "+
		        			addTable.getString(addTable.getColumnIndex("name"))+","+
							addTable.getString(addTable.getColumnIndex("path"))+","+
							addTable.getLong(addTable.getColumnIndex("time"))+","+
							addTable.getLong(addTable.getColumnIndex("size"))+","+
							addTable.getString(addTable.getColumnIndex("md5")));
		        	
		        	flyFileMessage.setMessage(add,
							        			addTable.getString(addTable.getColumnIndex("path")),
							        			addTable.getLong(addTable.getColumnIndex("time")),
							        			addTable.getString(addTable.getColumnIndex("md5")));
		        	
		        	logger.log("THE STRING BEING SENT TO SERVER: "+flyFileMessage.getREST());
		        	//logger.log("ADDING FILE DATA");
			        
					//send file message
			        logger.log("Adding File Message...");											//set CRC
					resultREST = myServer.contactServer(flyFileMessage.getREST(), String.valueOf(myServer.getCrcValue(hex)), hex);
					
					if(resultREST.isError() == false)
					{	 logger.log("Error Sending File Post Message");	}
					
					flyFileMessage.clearData();

		    		addTable.moveToNext();
		    		cursorCount--;
		        }
		        addTable.close();
		}
		
		logger.log("Finished End Pass");
		
		logger.log("=============================================Database Output========================================================");
		Cursor dbOutput = fileLog.getPrivateStoreDBoutput();
		int i = 1;
		dbOutput.moveToFirst();
        while (dbOutput.isAfterLast() == false)//fileTable loop
        {
        		logger.log("Row "+
        		dbOutput.getString(dbOutput.getColumnIndex("_id"))+" 	| "+
    			dbOutput.getString(dbOutput.getColumnIndex("name"))+" 	| "+
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
	}//end endpass
	
/**
 * This method passes the md5 created in MediaSync for a new file to fileLog to be stored
 * 
 * @param _md5 md5 file value
 */
	public void setMd5(String _md5)
	{	//logger.log("Setting MD5");
		fileLog.setMd5(_md5);
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
		{setMessage( _target, _path, Tools.getDate(_time),  _md5);}
		
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
				stringREST.append(Server.RestElementSeparator);
				stringREST.append(type);
				stringREST.append(Server.RestElementSeparator);
				stringREST.append(Tools.getDate());
				stringREST.append(Server.RestElementSeparator);
				stringREST.append(target);
				stringREST.append(Server.RestElementSeparator);
				stringREST.append(path);
				stringREST.append(Server.RestElementSeparator);
				stringREST.append(timeOROldPath);
				stringREST.append(Server.RestElementSeparator);
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
