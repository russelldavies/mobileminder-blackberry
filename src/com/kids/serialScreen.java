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
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class serialScreen extends MainScreen
{
    private	Debug 			logger = Logger.getInstance(); 

	public serialScreen()
	{
		super( MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR );

		logger.log("serialScreen constructor");
		
        setTitle( "Welcome to Mobile Minder" );        
        add(new LabelField("Mobile Minder Serial Number"));
        add(new SeparatorField());
        
		displayScreen();        
	}

	private void displayScreen()
	{
		logger.log("Displaying Screen...");

		//Method to retrieve serial number from the database
		String serial = getSerial();
		logger.log("The serial retrieved from the DB is: "+serial);
        if ("0" == serial)
        {
        	logger.log("No serial number yet.");
        	add(new LabelField("No device serial number has been retrieved. Please ensure your internet connection is active, or try again in a few minutes"));
        }
        else
        {
        	Bitmap bmpMM = Bitmap.getBitmapResource("mmman.png");
    		HorizontalFieldManager hFields = new HorizontalFieldManager(HorizontalFieldManager.USE_ALL_HEIGHT
				  	  												  | HorizontalFieldManager.USE_ALL_WIDTH
				  	  												  | HorizontalFieldManager.NO_HORIZONTAL_SCROLL
											    				  	  | HorizontalFieldManager.NO_VERTICAL_SCROLL 
											    				  		);
    		
    		// Now put it on screen
    		hFields.add(new BitmapField(bmpMM));
    		
    		//Now add text
    		VerticalFieldManager vfm = new VerticalFieldManager(VerticalFieldManager.USE_ALL_HEIGHT
    														  | VerticalFieldManager.USE_ALL_WIDTH
    														  | VerticalFieldManager.FIELD_HCENTER
    														  );
    		vfm.add(new LabelField("Please go to the Mobile Minder website and enter"));
    		// Add space at start to simulate "centered" serial number
    		LabelField serialLabel = new LabelField("             "+serial);
    		// We want the serial number in bold
    		serialLabel.setFont(serialLabel.getFont().derive(Font.BOLD));
    		vfm.add(serialLabel);
    		vfm.add(new LabelField("into the registration field to register this device!"));
    		hFields.add(vfm);    		
    		
    		// Now add all the fields to the screen
    		add(hFields);    		
        }
       
        //Pop up a message containing the serial number
        //Dialog.inform("The serial number is: "+theSerial);        
	}

	/**
	 * Method connects to the database, and reads from the registration table to get the serial number
	 * @return theSerial - The serial number in String format
	 */
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