package com.spotlight.track;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.io.File;
import net.rim.device.api.io.FileInputStream;

import com.kids.net.ErrorMessage;
import com.kids.prototypes.Controllable;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;
import com.kids.prototypes.enums.COMMAND_TARGETS;


/*
import java.io.IOException;
import java.io.InputStream;

import com.kids.net.ErrorMessage;
import com.kids.prototypes.Controllable;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;
import com.kids.prototypes.enums.COMMAND_TARGETS;
import com.kids.prototypes.enums.IMAGE_TYPES;
*/
/**
 * This class is used to retrieve, edit and reference files on the device file system
 */
public class MediaSync extends Thread implements Controllable {
	
	private LocalDataAccess actLog;
	//private Context context;
	private FileInfoManager fileInfoManager;
	private final int time;
	//private ArrayList <String> fileDeleteQueue = new ArrayList<String>(); 
	Vector fileDeleteQueue = new Vector();
    private String hexString = "";
    private boolean isImage = false;
    private Debug logger = Logger.getInstance();
    private String SDCardpath = "SDCard/";	// NB: Media path is SDCard/blackberry/Videos etc

/**
 *This is a constructor for MediaSync, it initialises a context and a local storage for the FileInfoManager object  
 * 
 * @param inputcontext Interface to global environment that the current application is running in.
 * @param _localData local message storage.
 */
    public MediaSync(/*Context inputcontext,*/ LocalDataWriter _localData)
	{
		//context = inputcontext;
		fileInfoManager = new FileInfoManager(/*inputcontext,*/ _localData);
		//time = 1000 * 60 * 5; //5min
		//time = 1000 * 60 * 2; //2min
		//time = 1000 * 30; //30sec(debug)
		time = 1000 * 60 * 10;
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
	public MediaSync(/*Context inputcontext*/)
	{
		//context = inputcontext;
		time = 1000 * 60 * 5;
	}
	
/**
 * This method is used to orchestrate synchronisation between the device file system and the local file information storage.
 */	
	public void run()
	{
		while(true)
		{
			if(isSdPresent())//check for sdcard
			{
				File f;
				logger.log("startpass");		fileInfoManager.startPass();
				logger.log("digging dir");		digDir(FileSystemRegistry.listRoots());//TODO: digDir(Environment.getExternalStorageDirectory());
				logger.log("endpass");			fileInfoManager.endPass();
				logger.log("delete queue");		deleteFile();
			}
			
			try 
			{	Thread.sleep(time);} 
			catch (InterruptedException e)
			{	logger.log("error thrown mediasync thread: "+e);
				actLog.addMessage(new ErrorMessage(e));	}
		}
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
		{	
			//ArrayList <String> fileNOTDeleteQueue = new ArrayList<String>(); 
			Vector fileNOTDeleteQueue = new Vector();
			//Iterator<String> itr = fileDeleteQueue.iterator();
			Cursor itr = fileDeleteQueue.firstElement();//.iterator();
			
		    while(itr.hasNext())
		    {	String path = itr.next();
		    	File fileToDelete = new File(path);
		    	
		    	if(!fileToDelete.exists())
		    	{logger.log("File: "+path+" doesn't exist");}
		    	else
		    	{
					if(isSdPresent())//check for sdcard
					{
						deleted = fileToDelete.delete();
					}
					if(deleted)
					{	logger.log("File: "+path+" has been deleted");}
					else//can't perform deletion
					{	logger.log("File: "+path+" has NOT been deleted");
						fileNOTDeleteQueue.addElement(path);
					}
		    	}
		    }
		    //queue not deleted files
			fileDeleteQueue = fileNOTDeleteQueue;
			
			//re-mount gallery to remove the thumbnail of the deleted file
			// Is this necessary for Blackberry?
			//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			
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
 * @param e URI to the file system
 * @return ArrayList of all the files in the system
 */
	private void digDir(Enumeration e) 
	{
        String tempName="",content="";
        
        try
        {
        	if(isSdPresent() && null != e)//check for sdcard
        	{
        		
        		FileConnection fc = (FileConnection) Connector.open("file:///SDCard/");
                System.out.println("List of files and directories under SD CARD:");
                Enumeration filelist = fc.list("*" , true);
                while(filelist.hasMoreElements())
    			{
                    String name = (String) filelist.nextElement();
                    FileConnection filenames =(FileConnection) Connector.open("file:///SDCard/"+ name,Connector.READ_WRITE);
                    if(filenames.exists())
    				{
                        content="DONE\n" + name;
                        System.out.println("fileName:" + name );
                        InputStream input = filenames.openInputStream();
                        String file_test_result = (null==input?"OKAY":"NULL");//readImages(input);
    					if(file_test_result.equals("OKAY") | file_test_result=="OKAY")
    							content = "OKAY" 
    								+ name.substring(name.indexOf("/")+1) 
    								+ ",0,1," + file_test_result;                 
                        else if(file_test_result.equals("NULL") | file_test_result=="NULL")
    							content = "NULL" 
    								+ name.substring(name.indexOf("/")+1) 
    								+ ",0,2," + file_test_result;                       
                        else 
    							content = "NULL" 
    								+ name.substring(name.indexOf("/")+1)
    								+ ",0,3,ERROR";
                        filenames.close();
    					fc.close();                 
                    }
                }
        		
        		
        		////////////////////////////////////
		        for (int i = 0; i < files.length; i++) 
		        {
		                if (files[i].isDirectory())
		                {	digDir(files[i]); }
		                else if (files[i].isFile())
		                {
		                	isImage = false;
		                	tempName = files[i].getName().toUpperCase();
		                	logger.log("temp name: "+tempName);
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
			                	if(false == tempName.startsWith("."))//hidden file
			                	{
				                	if(false == files[i].getPath().contains(".thumbnails"))
				                	{
				                		//logger.log("Files list=Name :" + tempName+" :: Files list=Path :" + files[i].getAbsolutePath());
					                	//logger.log("Calling AddFile");
					                	boolean preformMd5 =  fileInfoManager.addFile(files[i]);
					                	
					                	if(preformMd5)
					                	{
					                		//logger.log("gen MD5");
					                		generateMd5(files[i]);
					                	}
					                	//isImage = false;
				                	}
			                	}
			                }
		                }
		        }
		        ///////////////////////////////////////////////////////
        	}
        }
        catch (Exception e) {actLog.addMessage(new ErrorMessage(e));}  
	}

/**
 * This method determines if their is a mounted SD Card in the device
 * @return true if their is a mounted SD Card
 */
	public static boolean isSdPresent() 
	{
		//return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		Enumeration rootEnum = FileSystemRegistry.listRoots();
		while (rootEnum.hasMoreElements()) {
		      String root = (String) rootEnum.nextElement();
		      if ( root.equalsIgnoreCase("SDCard/"))
		    	      return true;
		      else 	  return false;
		   } 

	}
	
/**
 * This method digs through the file system and retrieves a HEX string of a file, searching by it's name.
 * @param f URI to the file system
 * @param file name
 * @return the file formated to a HEX string
 */
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
	
/**
 * This method generates an MD5 value for a file
 * 
 * @param file input file
 */
	private void generateMd5(File file)
	{	  try 
		  {	InputStream fin = new FileInputStream(file);
		    java.security.MessageDigest md5er = MessageDigest.getInstance("MD5");
		    byte[] buffer = new byte[1024];
		    int read;
		    do 
		    { read = fin.read(buffer);
		      if (read > 0)
		      md5er.update(buffer, 0, read);
		    } while (read != -1);
		    fin.close();
		    byte[] digest = md5er.digest();
		    logger.log("generated MD5: "+byteArrayToHexString(digest));
		    fileInfoManager.setMd5(byteArrayToHexString(digest));//send MD5 formated to hex to fileInfoManager to be stored
		  }catch (Exception e) {actLog.addMessage(new ErrorMessage(e));}  
	}

/**
 * This method formats a file byte array into a hex string
 * 
 * @param _file input file
 * @return hex string of file
 */
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
	    
		logger.log("HEX of file from device"+fileHexString);
		return fileHexString;
	}
	
/**
 * This method formats a byte-array into a hex string
 * 
 * @param b byte-array
 * @return hex string
 */
	 public static String byteArrayToHexString(byte[] b) 
	 {
	    StringBuffer sb = new StringBuffer(b.length * 2);
		    for (int i = 0; i < b.length; i++) 
		    {	int v = b[i] & 0xff;
		    	if (v < 16){	sb.append('0');}
		    	sb.append(Integer.toHexString(v));
		    }
	    	return sb.toString().toUpperCase();
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

public boolean isTarget(COMMAND_TARGETS targets) {
	// TODO Auto-generated method stub
	return false;
}
	
}
