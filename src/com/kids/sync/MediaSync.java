package com.kids.sync;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import com.kids.Controllable;
import com.kids.Logger;
import com.kids.Data.FileInfoManager;
import com.kids.Data.MMLinkedList;
import com.kids.Data.Tools;
import com.kids.net.ErrorMessage;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.enums.COMMAND_TARGETS;

import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.device.api.io.File;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.ui.component.ObjectListField;

/**
 * This class is used to retrieve, edit and reference files on the device file system
 */
public class MediaSync extends Thread implements Controllable
{
	String[] _extensions={
							"PNG","JPG","JPEG","BMP","GIF","TIFF","JFIF","PSD","PSB","PSP","DNG","RAW"
							,"MP3","OGG","WMA"
							,"AVI","MKV"
						 }; //File extensions to filter by.
	private LocalDataReader actLog;
	//private Context context;
	String _currentPath; //The current path;
    
    ObjectListField _olf; //Lists fields and directories.
	private FileInfoManager fileInfoManager;
	private final int time;
	private final int syncTime = 1000 * 10;//60 * 10;
    //public static CharsetEncoder asciiEncoder = Charset.forName("ISO-8859-1").newEncoder();  // or "US-ASCII" for ISO Latin 1
	//private ArrayList <String> fileDeleteQueue = new ArrayList<String>();
	private MMLinkedList fileDeleteQueue = new MMLinkedList();
    private String hexString = "";
    private  boolean isImage = false;
    private Debug logger = Logger.getInstance();
    private MMTools tools = Tools.getInstance();
    //private RecursiveFileObserver mObserver;
    private boolean synced = false;
    private boolean sleeping = false;
    private boolean bootSync = true;
    private String rootDir = new String("file:///SDCard/");//(Environment.getExternalStorageDirectory().getPath());
    private static final char[] kDigits = 
	  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    

/**
 *This is a constructor for MediaSync, it initialises a context and a local storage for the FileInfoManager object  
 * 
 * @param inputcontext Interface to global environment that the current application is running in.
 * @param _localData local message storage.
 */
    public MediaSync(LocalDataReader _localData)
	{
		fileInfoManager = new FileInfoManager(_localData);
		//time = 1000 * 60 * 5; //5min
		time = 1000 * 60 * 2; //2min
		//time = 1000 * 30; //30sec(debug)
		logger.log("Starting.. MediaSync");
		this.start();
	}
    
/**
 *This is a constructor for MediaSync, it initialises a context and a local storage for the FileInfoManager object. 
 *This constructor creates a thread that is not initially started.
 * 
 * @param inputcontext Interface to global environment that the current application is running in.
 * @param _localData local message storage.
 */
	public MediaSync()
	{
		time = 1000 * 60 * 5;
	}
	
/**
 * This method is used to orchestrate synchronisation between the device file system and the local file information storage.
 */	
	public void run()
	{
		sleeping = true;
		
		if(!bootSync)
		{	
			logger.log("MediaSync Sleeping");
			try 
			{
				Thread.sleep(syncTime);
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}
			logger.log("MediaSync Waking Up");	
		}
		
		synced = false;
		int passes = 0;
		while(true != synced)
		{
			passes++;
			try
			{
				if(Tools.isSdPresent())//check for sdcard
				{
					logger.log("startpass...");		
					fileInfoManager.startPass();
					logger.log("digging dir...");		
					//digDir(Environment.getExternalStorageDirectory());
					digDir(rootDir);
					logger.log("endpass...");			
					fileInfoManager.endPass();
					logger.log("delete queue...");		
					deleteFile();
					synced = true;
				}
			}
			catch (Exception e)
			{	
				logger.log("error thrown mediasync thread: "+e);
				actLog.addMessage(new ErrorMessage(e));
				synced = false;
			}
			
			if(3 == passes)
			{	
				synced = true;
				this.interrupt();
				break;
				
			}
		}
		sleeping = false;
		bootSync = false;
		//return synced;
	}
	
/**
 * This method stores the path of a file to be deleted.
 * 
 * @param _file file path
 * @return	true if file was stored
 */
	public boolean queueDeleteFile(String _file)
	{
		logger.log("File path in delete queue"+_file);
		fileDeleteQueue.add(_file);
		return true;
	}
	
/**
 * This method recursively deletes the file(s) listed in the delete queue based on their path.
 */
	public void deleteFile()
	{
		boolean deleted = false;
		if(!(0 == fileDeleteQueue.size()))//file(s) to be deleted
		{	//ArrayList <String> fileNOTDeleteQueue = new ArrayList<String>();
			MMLinkedList fileNOTDeleteQueue = new MMLinkedList();
			File fileToDelete;
			//Iterator<String> itr = fileDeleteQueue.iterator();
			
		for (MMLinkedListIterator theItr = fileDeleteQueue.getIterator(); theItr.isValid(); theItr.advance())
		    {	
				String path = (String) theItr.retrieve();
				//check for duplicate file
				String[] paths = fileInfoManager.findDuplicate(path);
				logger.log("Dupliacte Path Array Size: "+paths.length);
				//delete each file in duplicate array
				for(int count = 0; count < paths.length; count++)
				{
			    	fileToDelete = new File(paths[count]);
			    	
			    	if(!fileToDelete.exists())
			    	{logger.log("File: "+paths[count]+" doesn't exist");}
			    	else
			    	{
						if(Tools.isSdPresent())//check for sdcard
						{
							deleted = fileToDelete.delete();
						}
						if(deleted)
						{	logger.log("File: "+paths[count]+" has been deleted");}
						else//can't perform deletion
						{	logger.log("File: "+paths[count]+" has NOT been deleted");
							fileNOTDeleteQueue.add(paths[count]);
						}
			    	}
		    	}
		    }
		    //queue not deleted files
			fileDeleteQueue = fileNOTDeleteQueue;
			
			//re-mount gallery to remove the thumbnail of the deleted file
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, URI.parse("file://" + Environment.getExternalStorageDirectory())));
			
			/*
			//Implementation to leave the thumbnail still in the gallery, but the user can not view,send etc. the image
			if(deleteDir(new File(Environment.getExternalStorageDirectory()+"/DCIM/.thumbnails")))
			{logger.log("removing cached thumbnails...");}
			if(deleteDir(new File(Environment.getExternalStorageDirectory()+"/media/external/images/thumbnails")))
			{logger.log("removing cached media thumbnails...");}*/
		}
	}
	
/*
//This method is used to delete a directory and all it's child files in the device file system
public static boolean deleteDir(File dir) { 
    	

        if (dir!=null && dir.isDirectory()) { 
            String[] children = dir.list(); 
            for (int i = 0; i < children.length; i++) { 
                boolean success = deleteDir(new File(dir, children[i])); 
                if (!success) { 
                    return false; 
                } 
            } 
        } 
        // The directory is now empty so delete it 
        return dir.delete(); 
    } 
*/
	
	/**
	 * This method digs through the file system and retrieves all the files	
	 * @param path - String path to the start directory
	 * @return Vector of all the files in the system
	 */
		private void digDir(String path) 
		{	    
	        Enumeration fileEnum;
	        Vector filesVector = new Vector();

	        _currentPath = path;

	        if (path == null)
	        {
	            //Read the file system roots.
	            fileEnum = FileSystemRegistry.listRoots();

	            while (fileEnum.hasMoreElements())
	            {
	                filesVector.addElement((Object)fileEnum.nextElement());
	            }
	        }
	        else
	        {
	            //Read the files and directories for the current path.
	            try
	            {
	                FileConnection fc = (FileConnection)Connector.open(path);
	                fileEnum = fc.list();
	                String currentFile;

	                while (fileEnum.hasMoreElements())
	                {
	                    //Use the file extension filter, if there is one.
	                    if (_extensions == null)
	                    {
	                        filesVector.addElement((Object)fileEnum.nextElement());
	                    }
	                    else
	                    {
	                        currentFile = ((String)fileEnum.nextElement());

	                        if (currentFile.lastIndexOf('/') ==
	                           (currentFile.length() - 1))
	                           {
	                           //Add all directories.
	                           filesVector.addElement((Object)currentFile);
	                           }
	                        else
	                        {
	                            //This is a file. Check if its
	                            //extension matches the filter.
	                            for (int count = _extensions.length - 1;
	                                  count >= 0; --count)
	                            {
	                                if (currentFile.indexOf(_extensions[count]) != -1)
	                                 {
	                                      //There was a match, add the file and
	                                      //stop looping.

	                                      filesVector.addElement((Object)
	                                          currentFile);
	                                      break;
	                                 }
	                            }
	                       }
	                  }
	                }
	             }
	              catch (Exception ex)
	              {
	                  //Dialog.alert("Unable to open folder. " + ex.toString());
	              }
	        }
	        //return filesVector;
		  /*  
		    _olf = new ObjectListField();
			ServiceBook sb = ServiceBook.getSB();
			ServiceRecord[] srs = sb.getRecords();

			for(int cnt = srs.length - 1; cnt >= 0; --cnt)
			{
				// Get a starting point on the file system
				ServiceConfiguration sc = new ServiceConfiguration(srs[cnt]);
				Store store = Session.getInstance(sc).getStore();
				
				// Get a list
				Folder[] folders = store.list();
				for( int foldercnt = folders.length - 1; foldercnt >= 0; --foldercnt)
				{
					Folder f = folders[foldercnt];
					recurse(f);
				} // end for()
			}  // end for()
			
			/////////////////////////////////////
			//////////////////////////////////
			///////////////////////////////////
			/*
	        File[] files = f.listFiles();
	        String tempName;
	        boolean preformMd5;
	        try
	        {
	        	if(Tools.isSdPresent() && null != files)//check for sdcard
	        	{
			        for (int i = 0; i < files.length; i++) 
			        {
			                if (files[i].isDirectory() && !(files[i].getName().equalsIgnoreCase("data")) && !(files[i].getName().equalsIgnoreCase("music")))
			                {	digDir(files[i]);	}
			                else if (files[i].isFile())
			                {
				                if(isPureAscii(files[i].getAbsolutePath()))
				                {	
				                		isImage = false;
					                	tempName = files[i].getName().toUpperCase();
					                	//logger.log("temp name: "+tempName);
					          		 for (IMAGE_TYPES  img : IMAGE_TYPES.values())
					        		  {
							                	//logger.log("image types compare: "+img.name());
						          			if(tempName.endsWith(img.name()))
						          			{
						          				isImage = true;
						          				break;
						          			}
					        		   }
					          		 
						                if(true == isImage)
						                {
						                	if(!tempName.startsWith("."))//) && (tempName.contains("IMG")))//hidden file
						                	{
							                	if(!files[i].getPath().contains(".thumbnails"))
							                	{
							                		//logger.log("Files list=Name :" + tempName+" :: Files list=Path :" + files[i].getAbsolutePath());
								                	//logger.log("Calling AddFile");
								                	preformMd5 =  fileInfoManager.addFile(files[i]);
								                	if(preformMd5)
								                	{
								                		//
								                		generateMd5(files[i]);
								                		//logger.log("sleep!");
								                		//Thread.sleep(15000);
								                		//logger.log("awake!");
								                	}
								                	tempName = null;
								                	
								                	//isImage = false;
							                	}
						                	}
						                }
				                }
			                }
			        }
	        	}
	        	
	        	files = null;
	        }
	        catch (Exception e) 
	        {
	        	e.printStackTrace();
	        	actLog.addMessage(new ErrorMessage(e));
	        }*/  
		}
		
		public String[] recurse(Folder f)
		{
			String[] folderList = null;
			String meh = f.getFullName();
		   
		   Folder[] farray = f.list();
		   //Search all the folders sub-folders
		   for (int fcnt = farray.length - 1; fcnt >= 0; --fcnt)
		   {
			   recurse(farray[fcnt]);
		   }
		   return folderList;
		}
		
 /**
  * This method has been overridden from the FileObservable interface.
  * By implementing this interface this class can receive FileChange arguments.
  * These commands are then processed within this method.
  * 
  * @param inputArgs arguments sent specifying the FileChange instructions from the server.
  * @return true if the command has been processed with out any errors.
  */
	//@Override
	public boolean processFileCommand(String args) 
	{
		if(!sleeping)
		{	run();	}
		return synced;
	}
		

/*
/**
 * This method digs through the file system and retrieves a HEX string of a file, searching by it's name.
 * @param f URI to the file system
 * @param file name
 * @return the file formated to a HEX string
 
	public String findFile(File f, String name)
	{	File[] files = f.listFiles();
		if(isSdPresent() && null != files)//check for sdcard
		{
	        for (int i = 0; i < files.length; i++) 
	        {		if (files[i].isDirectory()){findFile(files[i], name);}
	        
	                else if (files[i].isFile() && files[i].getName().equals(name))
	                {	//logger.log("File Found!= Name :" + files[i].getName()+" :: Path :" + files[i].getAbsolutePath());
	                	hexString = getHexFileString(files[i]);
	                }	              
	        }
		}
        return hexString;
	}
	*/
	
/**
 * This method generates an MD5 value for a file
 * 
 * @param file input file
 */
	private void generateMd5(File file)
	{	
		InputStream fin = null;
		try
		  {	
			fin = new FileInputStream(file);
		    MessageDigest md5er = MessageDigest.getInstance("MD5");
		    byte[] bufferNdigest = new byte[1024];
		    int read;
		    do 
		    { read = fin.read(bufferNdigest);
		      if (read > 0)
		      {md5er.update(bufferNdigest, 0, read);}
		    } 
		    while (read != -1);

		    bufferNdigest = md5er.digest();
		    
		    logger.log("generated MD5: "+byteArrayToHexString(bufferNdigest));
		    fileInfoManager.setMd5(byteArrayToHexString(bufferNdigest));//send MD5 formated to hex to fileInfoManager to be stored
		    md5er = null;
		    bufferNdigest = null;
		    
		  }
		
		  catch (Exception e) 
		  {actLog.addMessage(new ErrorMessage(e));} 
		  
		  finally 
		    {	
		    	try 
		    	{	if (fin != null) 
			    	{	fin.close();	}	
		    	} 
		    	catch (IOException e) 
		    	{	actLog.addMessage(new ErrorMessage(e));	}	
		    }
		  
	}
/*
/**
 * This method formats a file byte array into a hex string
 * 
 * @param _file input file
 * @return hex string of file
 
	private String getHexFileString(File _file)
	{
        byte[] byteStream = new byte[(int) _file.length()];
        FileInputStream fis = null;	
        String fileHexString = "";
        try
        {
			fis = new FileInputStream(_file);
	        fis.read(byteStream);
	        fis.close();
	        fileHexString = byteArrayToHexString(byteStream);
		}
		catch (FileNotFoundException e1) 
		{	actLog.addMessage(new ErrorMessage(e1));	}
        catch (IOException e2) 
        {	actLog.addMessage(new ErrorMessage(e2));	}
	    catch(OutOfMemoryError e3)
	    {	actLog.addMessage(new ErrorMessage(e3));	}
	    
		//logger.log("HEX of file from device"+fileHexString);
		return fileHexString;
	}
*/
/**
 * This method formats a byte-array into a hex string
 * 
 * @param b byte-array
 * @return hex string
 */
	 public String byteArrayToHexString(byte[] b) 
	 {
	    char[] hexVal = new char[b.length * 2];
	    int value = 0;
	    for (int i = 0; i < b.length; i++) 
	    {
	         value = (b[i] + 256) % 256;
		     hexVal[i * 2 + 0] = kDigits[value >> 4];
		     hexVal[i * 2 + 1] = kDigits[value & 0x0f];
	    }
    	return new String(hexVal);
	 }		 

	
 /**
  * This method has been overridden from the Controllable interface.
  * By implementing this interface this class can receive command arguments.
  * These commands are then processed within this method.
  * 
  * @param inputArgs arguments sent specifying the command instructions from the server.
  * @return true if the command has been processed with out any errors.
  */
	//@Override
	public boolean processCommand(String[] inputArgs) 
	{	logger.log("Processing Delete File Command...");
		boolean complete = false;
		logger.log("args[1]:Path = "+inputArgs[1]);
			if(inputArgs[0].equalsIgnoreCase("del"))
				{complete = queueDeleteFile(new String(inputArgs[1]));}//delete file
			
		return complete;
	}

/**
 * This method has been overridden from the Controllable interface.
 * By implementing this interface this class can specify the type of commands it can process.
 * 
 * @param target passed to be checked.
 * @return true if this is the desired target.
 */
	//@Override
	public boolean isTarget(COMMAND_TARGETS targets) 
	{
		if(targets == COMMAND_TARGETS.FILES)
		{return true;}
		else 
		{return false;}
	}
	
/**
 * This method has been overridden from the FileObservable interface.
 * By implementing this interface this class can specify the type of commands it can process.
 * 
 * @param target passed to be checked.
 * @return true if this is the desired target.
 */
	//@Override
	public boolean isTarget(OBSERVERS targets) 
	{
		if(targets == OBSERVERS.FILECHANGES)
		{return true;} 
		else 
		{return false;}
	}

   
/**
 * This method determines if the input string contains any non-ascii characters
 * @param input String to be checked
 * @return true if string is pure ascii
 */
	  public boolean isPureAscii(String input) 
	  {		return asciiEncoder.canEncode(input); }
	
}