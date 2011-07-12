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
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class serialScreen extends MainScreen
{
    private	Debug 			logger = Logger.getInstance(); 

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
		
		String serial = getSerial();
		
    	logger.log("Serial number is: "+serial);
        add(new LabelField("Please go to the Mobile Minder website and enter: "
							+serial
							+" in the registration field to register this device!"
						  )
        	);
		/*
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
        }*/
       
        //Pop up a message containing the serial number
        //Dialog.inform("The serial number is: "+theSerial);
        
	}

	private String getSerial()
	{
		String theSerial=null;
		try {
			URI theURI = URI.create("file:///SDCard/Databases/MobileMinder/CVKe");
			logger.log("serialScreen::Opening DB...");			
			Database db = DatabaseFactory.open(theURI);
			logger.log("serialScreen::DB OPEN!");
			Statement st = db.createStatement("SELECT * FROM regDB");
			st.prepare();
			Cursor cursor = st.getCursor();
			cursor.first();
			Row row = cursor.getRow();
			theSerial = row.getString(1);
			st.close();
			cursor.close();
			logger.log("serialScreen::Closing DB...");
			db.close();
			logger.log("serialScreen::DB Closed!");
		} catch (IllegalArgumentException e) {
			logger.log("x::serialScreen::getSerial::IllegalArgumentException::"+e.getMessage());
			e.printStackTrace();
		} catch (MalformedURIException e) {
			logger.log("x::serialScreen::getSerial::MalformedURIException::"+e.getMessage());
			e.printStackTrace();
		} catch (ControlledAccessException e) {
			logger.log("x::serialScreen::getSerial::ControlledAccessException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabaseIOException e) {
			logger.log("x::serialScreen::getSerial::DatabaseIOException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabasePathException e) {
			logger.log("x::serialScreen::getSerial::DatabasePathException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabaseException e) {
			logger.log("x::serialScreen::getSerial::DatabaseException::"+e.getMessage());
			e.printStackTrace();
		} catch (DataTypeException e) {
			logger.log("x::serialScreen::getSerial::DataTypeException::"+e.getMessage());
			e.printStackTrace();
		}
		
		return theSerial;
	}
}