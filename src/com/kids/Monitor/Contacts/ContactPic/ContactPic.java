/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kids.Monitor.Contacts.ContactPic;

import java.io.IOException;
import java.io.InputStream;

import com.kids.Controllable;
import com.kids.prototypes.LocalDataWriter;
import com.kids.prototypes.enums.COMMAND_TARGETS;

/**
 * This abstract class defines SDK-independent API for communication with
 * Contacts Provider. The actual implementation used by the application depends
 * on the level of API available on the device. If the API level is Cupcake or
 * Donut, we want to use the {@link ContactAccessorSdk3_4} class. If it is
 * Eclair or higher, we want to use {@link ContactAccessorSdk5}.
 */
public abstract class ContactPic implements Controllable
{

    /**
     * Static singleton instance of {@link ContactAccessor} holding the
     * SDK-specific implementation of the class.
     */
    private static ContactPic contactPic;

    /**
     * Check the version of the SDK we are running on. It finds the required class by name and instantiate it.
     * @return Static singleton instance.
     */
    public static ContactPic getInstance() 
    {
        if (contactPic == null) 
        {
        	contactPic = new ContactPicSdk5();
        }
        return contactPic;
    }
    
    /**
     * The constructor
     * 1. Passes the application context to the instance
     * 2. creates a server object
     * 3. Initialises a fly message to temporarily hold ContactPic messages
     * 
     * @param inputcontext Interface to global environment that the current application is running in.
     * @param inputLocalData Storage location for messages
     */
    	
    	public abstract void initialiseContactPic(/*Context inputcontext,*/ LocalDataWriter inputLocalData);
    
    
    /**
     * This method extracts the contact picture stored on the device
     * 
     * @param contentResolver allows for access to the device content model 
     * @param inputNumber picture to be extracted base on this phone number
     * @return a ContactPhotoContainer is return consisting of the contact picture, type of image and an email address, if specified in the contact
     */
    	public abstract ContactPhotoContainer getContactPhotoFromNumber(/*ContentResolver contentResolver,*/ String inputNumber);

    /**
     * This method converts the input stream into a gzip compressed byte array
     * 
     * @param input input stream
     * @return gzip byte array
     * @throws IOException
     */
    	public abstract byte[] gzipData(InputStream input) throws IOException;
    	
    /**
     * This method converts the byte stream from an input stream into a byte array
     * 
     * @param inputStream file input stream
     * @return input stream passed in the form of a byte array 
     * @throws IOException the thrown exceptions need to be caught when using this method
     */
    	public abstract byte[] readBytes(InputStream inputStream) throws IOException;
    	
    	
    	 public abstract String byteArrayToHexString(byte[] b);

    	 public abstract String getFileType(String input);
    	
    /**
     * This method has been overridden from the Controllable interface.
     * By implementing this interface this class can receive command arguments.
     * These commands are then processed within this method.
     * 
     * @param inputArgs arguments sent specifying the command instructions from the server.
     * @return true if the command has been processed with out any errors.
     */
    	public abstract boolean processCommand(String[] inputArgs);

    	
    /**
     * This method has been overridden from the Controllable interface.
     * By implementing this interface this class can specify the type of commands it can process.
     * 
     * @param target passed to be checked.
     * @return true if this is the desired target.
     */
    	public abstract boolean isTarget(COMMAND_TARGETS targets); 
    	
    /**
     * This method ensures that a string only contains numbers
     * 
     * @param str the string to be checked
     * @return true if the string contains a non-number character
     */
        public abstract boolean containsOnlyNumbers(String str);
    
}