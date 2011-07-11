package com.kids;

import com.kids.prototypes.Debug;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.database.DatabaseIOException;
import net.rim.device.api.database.DatabasePathException;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class serialScreen extends MainScreen
{
    private 			 Debug 	logger 		   = Logger.getInstance();

	public serialScreen()
	{
		super( MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR );

		logger.log("serialScreen constructor");
		
        setTitle( "HelloBlackBerry" );        
        add(new LabelField("Mobile Minder Serial Number"));
        add(new SeparatorField());

		displayScreen();        
	}

	private void displayScreen()
	{
		logger.log("Displaying Screen...");
		//String theSerial = Registration.getRegID();
		
		//Get the serial number directly from the DB
		RegData regData = new RegData();
		String theSerial = regData.getRegSN();

		logger.log("The serial retrieved from the DB is: "+theSerial);
        if ("0" == theSerial)
        {
        	logger.log("No serial number yet.");
        	add(new LabelField("No device serial number has been retrieved. Please ensure your internet connection is active, or try again in a few minutes"));
        }
        else
        {
        	logger.log("Serial number is: "+theSerial);
            add(new LabelField("Please go to the Mobile Minder website and enter: "
								+theSerial
								+" in the registration field to register this device!"
							  )
            	);
        }
       
        //Pop up a message containing the serial number
        //Dialog.inform("The serial number is: "+theSerial);
        
	}
}