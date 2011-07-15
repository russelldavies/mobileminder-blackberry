package com.kids;

import net.rim.blackberry.api.phone.Phone;
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
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.DeviceInfo;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMServer;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;
import com.kids.Data.Tools;
import com.kids.net.Reply;
import com.kids.net.Server;

/**
 * This class checks the registration stage that currently the device is in.
 */
public class Registration extends Thread
{
    private 	  	boolean 		regOK = false;
    private			boolean 		messageSent = false;
	private 		LocalDataReader actLog;
    public static 	RegData 		regData;
    public static 	String 			regID = "0";
    private 		MMServer 		server;
    private	final	int				sleepTimeLong	= 1000*60*60*24;//24h
    private	final	int				sleepTimeShort 	= 6000;		//6sec
    private final 	int 			finePhoneNum_timeOut = 10;
    private static 	Debug 			logger = Logger.getInstance();
	private 		MMTools 		tools = Tools.getInstance();
 
  /**
   * This constructor initialises the context parameter, creates its own instance of Server and regData.
   * It requests a Registration ID and the registration stage of the current device from the web server.
   * 
   * @param _context Interface to global environment that the current application is running in.
   * @param _actLog Instance of the LocalDataAccess class that is passed as a parameter to instantiate that Server.
   */
    public Registration(LocalDataReader _actLog)
    {
		new mmNotification();
    	actLog 	  = _actLog;
		server 	  = new Server(_actLog);
		regData   = new RegData();
		regID	  = regData.getRegSN();
		stageState(regData.getStageValue());
		this.start();
    }
   
    /**
     * This method constantly checks the account status of the device at defined intervals.
     * Checks if the sim of the device has been unlocked. 
     */
    public void run()
    {
    	logger.log("Reg now running");
    	boolean newState = true;
    	Reply 	response;
    	int 	nextStage;
    	int 	currentStageValue = regData.getStageValue();
    	int		time 			  = 0;
    	int 	finePhoneNum 	  = 0;

		while(phoneNumber().equals("0")//if the SIM is not setup before the device starts(i.e. SIM lock) then ANDROID will return null for the PhoneNum
		&& finePhoneNum_timeOut > finePhoneNum)
		{
			logger.log("waiting for Phone's Number");
			//phoneNum = telephonyMgr.getLine1Number();
			//phoneNum = phoneNumber();
    		try 
    		{//Socket is removed while application is idle to save resources on the device
				Thread.sleep(sleepTimeShort);//1min	
			} 
    		catch(InterruptedException e) 
    		{	
    			//TODO:
    			//actLog.addMessage(new ErrorMessage(e));
    			break;	
    		}
    		
			finePhoneNum++;
		}
		logger.log("data.getStageValue = "+regData.getStageValue());
    	while(regData.getStageValue() < 2)
    	{
    		logger.log("WHILE REG!!");
    		
    		currentStageValue = regData.getStageValue();
    		logger.log("REG asking server");
    		response  = requestNextStage(currentStageValue);//send current stage 
    		logger.log("REG server says: "+response.getREST());

    		if(response.isError())
			{	
    			logger.log("Reg Hit Error");
    			newState = false;
    			time = sleepTimeShort;
   			}
    		else
    		{
    			logger.log("ASKING SERVER FOR REG!!: "+response.getInfo());
    			//saves the new stage from the reply message
    			nextStage = tools.txt2num(response.getInfo());
    			if(currentStageValue == nextStage)
    			{	
    				logger.log("REG: currentStageValue == nextStage"+currentStageValue+"=="+nextStage);
    				newState = false; 
    				//Added this line in cause regID was staying at 0 when app icon is clicked
    				//regData.setRegSN(response.getRegID());
    				if(currentStageValue < 2)//Just waiting to reg online
    				{	time = sleepTimeShort;	}
    				else
    				{	time = sleepTimeLong;	}
    			}
    			else
    			{
    				logger.log("REG: currentStageValue != nextStage "+currentStageValue+"!="+nextStage);
    				newState = true;
    				if(0 == currentStageValue)
					{	
    					logger.log("REG: currentStageValue = "+currentStageValue);
						regID = response.getRegID();
						regData.setRegSN(regID);
	    			}
    				//assigns new stage
					regData.setStageValue(nextStage);//saves stage to memory
					//if("0" != regData.getRegSN()) regData.setRegSN(regID);
    				messageSent = false; // This handles the user getting a new TXT with stage details
					stageState(nextStage);//process stage
    			}
    		}

	    	if(!newState)
	    	{
	    		logger.log("REG: newState = true");
	    		try 
	    		{
	    			logger.log("RegSleep:"+time);
	    			//ser.shutdown();
					Thread.sleep(time);//1Day
					//ser.startup();
					logger.log("RegWalk");
				} 
	    		catch(InterruptedException e) 
	    		{	
	    			logger.log("x::Registration::run()::Exception::"+e.getMessage());
	    			break;	
	    		}
	    	}
	    	//update the registration serial number in the DB
	    	regData.setRegSN(regID);
    	}   	
    	
    }
    
    /**
     * Returns phone number for line1. If it is unavailable then returns null.
     * @return A String of phone number. Null if line is unavailable.
     */
    private String phoneNumber()
    {
    	if (null == Phone.getDevicePhoneNumber(false))
    	{ return "0"; }
    	else
    	{ return Phone.getDevicePhoneNumber(false); }
    }

    /**
     * Returns the unique phone ID
     * @return
     */
    private String phoneID()
    {
    	// Possible issue: http://stackoverflow.com/questions/680782/getting-device-imei
    	String phoneID  = System.getProperty("imei");
    	
    	if (null == phoneID)
    	{
    		//phoneID=System.getProperty("meid");
    		phoneID=CDMAInfo.getDecimalMEID();
    		// If its STILL null, just set the ID to 0
    		if (null == phoneID) phoneID="0";
    	}

		return phoneID;
    }
    /**
     * This method acts as a lookup for stages for the current device to display the registration stage to the user.
     * 
     * @param inputStage stage of registration
     */
    private void stageState(int inputStage)
    {
    	String  stateText = "";
    	
    	switch(inputStage) 
    	{
			case 0: //New install
				stateText = "Requesting Serial Number...\nPlease ensure you are connected to the internet";
				logger.log("In switch: value is: 0");
    			break;
    		case 1://New & has SN
    			stateText = "New Mobile Minder serial number retrieved!";
    			logger.log("In switch: value is: 1");
    			break;
    		case 2: //Wed Reg
    			stateText = "This is a trial account!";
    			logger.log("In switch: value is: 2");
    			regOK = true;
    			break;
    		case 3: //Device Reg
    			stateText = "Mobile Minder is now fully Active!";
    			logger.log("In switch: value is: 3");
    			regOK = true;
    			break;
    		//default:break;
    	}
    	
    	logger.log("After switch. Value was: "+inputStage);
    	logger.log(stateText);
		// Update the user notification in the global inbox
    	// This message will contain the regID so they can register online.
		//mmNotification.addMsgToInbox(stateText,inputStage,regID);
     }
    
    /**
     * This method sends a request to the server with device information and gets a return reply.
     * 
     * @param _currentStage current registration stage of the device.
     * @return reply message which contains the registration stage of the account.
     */
    private Reply requestNextStage(int _currentStage)
    {
    	//server.startup();
    	logger.log("requestNextStage1");
    	Reply result = server.contactServer(new RegistrationMessage(_currentStage,phoneNumber(),phoneID(),getDeviceManufacturer()));
    	logger.log("requestNextStage2");
    	//server.shutdown();
    	return result;
    }
    
    /**
     * This method is used to retrieve the device registration id, it can be used from anywhere in the system.
     * 
     */
   // public static String getRegID()
    public static String getRegID()
    {	
    	if (regID == "0")
    	{
    		if (null == regData)
    			regData   = new RegData();
    		logger.log("Serial number is 0. Updating...");
    		regData.getRegSN();
    	}
    	return new String(regID); //returns a copy so that the value cannot be modified. 
    }
    
    /**
     * This method is used to find the status of the device registration.
     * 
     * @return if the device has completed the registration stage.
     */
    public boolean regOK()
    {	return regOK; }
    
    /**
     *This method retrieves the device manufacturer information.
     *
     * @return manufacturer information.
     */
    private static String manufacturer = null;
    private String getDeviceManufacturer()// throws Exception 
    {
        // use reflection to get device manufacturer safely.

        // ANDROID1.6
        // android.os.Build.MANUFACTURER

        if (manufacturer != null) 
        {  return manufacturer;   }

    	// Device info - http://blog.vimviv.com/blackberry/blackberry-device-information-api/
    	// Get unique hardware identifier that identifies the exact handset
    	//java.lang.Integer.toHexString(DeviceInfo.getDeviceId())  <- BB PIN
    	manufacturer="Blackberry" + DeviceInfo.getDeviceId()    // Unique ID/PIN
    							  + Branding.getVendorId()       // Vendor ID (Vodafone/O2/etc)
    							  ;
  
        return manufacturer;
    } 
}

/**
 *This class is an implementation of Message. It specifies the parameters and methods implementation for RegitrationMessage.
 * 
 *
 */
class RegistrationMessage implements Message
{
	private final static int type = 9;
	private final  int mmVERSION = 2;
	private 		boolean error;
	private 		String 	deviceTime; 
	private 		int		stage;	
	private 		String	phoneNum; 	
	private 		String	deviceID;
	private 		String 	info;
	private 		String 	manufacturer;
	private MMTools tools = Tools.getInstance();

	/**
	 * This the constructor of RegistrationMessage. It sets the values for RegistrationMessage.
	 * @param inputStage Current registration stage of the device.
	 * @param inputPhoneNum Phone number of the device.
	 * @param inputDeviceID Device identification number.
	 * @param inputManufacturer Device manufacturer information.
	 */
	public RegistrationMessage(int inputStage, 
								String inputPhoneNum, 
								String inputDeviceID, 
								String inputManufacturer)
	{
		error 		 = false;
		deviceTime 	 = tools.getDate();
		stage 		 = inputStage;
		manufacturer = inputManufacturer;
		/*
		logger.log("inputPhoneNum"+inputPhoneNum);
		
		if(null == inputPhoneNum)
		{	phoneNum = "";}
		else
		{*/	phoneNum = inputPhoneNum;/*}
		logger.log("phoneNum:"+phoneNum);
		if(null == inputDeviceID)
		{	deviceID = "";}
		else
		{*/	deviceID = inputDeviceID;//}
		
		info 		= "Blackberry";
	}
	
	/**
	 * This method retrieves error status of the RegistrationMessage.
	 * 
	 * @return error status.
	 */

	public boolean getError() 
	{	return error;	}
	
	/**
	 * This method retrieves information body of RegistrationMessage.
	 */

	public String getInfo() 
	{	return info;	}
	/**
	 * This method formats all the parameters of the RegistrationMessage into a single string.
	 * 
	 * @return It returns registration message as a single string.
	 */
	//@Override
	public String getREST()
	{	//RegSN,	reg, 	error, deviceTime, stage,	PhoneNum, 	DeviceID, info
		// TODO: Change to String builder
		return 	Registration.getRegID() 	+ Tools.RestElementSeparator +
				'0'+type					+ Tools.RestElementSeparator +
				//(error?1:0)					+ Tools.RestElementSeparator +
				deviceTime					+ Tools.RestElementSeparator +
				stage						+ Tools.RestElementSeparator +
				phoneNum					+ Tools.RestElementSeparator +
				deviceID					+ Tools.RestElementSeparator +
				manufacturer				+ Tools.RestElementSeparator +
				DeviceInfo.getDeviceName()	+ Tools.RestElementSeparator +
				Tools.getOSVersion()		+ Tools.RestElementSeparator +
				mmVERSION					+ Tools.RestElementSeparator +
				info;
	}
	
	/**
	 * This method retrieves the time when the registrationMessage was created.
	 * 
	 * @return a string contains the time.
	 */
	//@Override
	public String getTime() 
	{	return deviceTime;	}
	
	/**
	 * This method retrieves the type of the registrationMessage type.
	 * 
	 * @return a string contains the type.
	 */
	//@Override
	public int getType()
	{	return type;	}
}

/**
 * 
 * This class creates a local storage for the registration stage value and regID.
 *
 */
class RegData
{
    public static Debug logWriter = Logger.getInstance();

	private static 	  	 int 	currentState;
	private static 	  	 String	currentSerial;
	public  final static String DATABASE_NAME    = "CVKe";
    public  final static String DATABASE_TABLE   = "regDB";
    private final static String KEY_STAGE		 = "Stage";
    private final static String KEY_NUMBER	  	 = "Number";
    private final static String DATABASE_CREATE  = "create table `"+DATABASE_TABLE+"` ("
														 		   +"`"+KEY_STAGE +"` integer NOT NULL,"
														 		   +"`"+KEY_NUMBER+"` TEXT	NOT NULL);";

    public static String   DATABASE_LOCATION = "file:///SDCard/Databases/MobileMinder/";
    public 		  Database storeDB			 = null;
    public static URI      dbURI;
    
    public static boolean sdCardPresent	= false;	// Bool to keep track of when SD Card is mounted
    public static boolean dbExist		= false;	// For checking to see if the DB already exists before each DB call

    /**
     * This is the constructor regData. It creates the environment for the local database and sets current registration stage.
     * 
     * @param inputContext Interface to global environment that the current application is running in.
     */
    public RegData() 
    {
    	logWriter.log("RegData constructor");
    	//storeDB  = new Database();//new DatabaseHelper();
    	currentState = getStageValueDB();
    	currentSerial = getRegSN();
    	Registration.regID = currentSerial;
    }
    
    /**
     * Method that sets the URI of the database, so it can be used with DatabaseFactory
     */
	private void getdbURI()
	{
		logWriter.log("In RegData getdbURI method");
		try {
			dbURI = URI.create(DATABASE_LOCATION+DATABASE_NAME);
			logWriter.log("RegData::getdbURI::URI"+(null==dbURI ? " is not " : " is ")+"usable");
		} catch (IllegalArgumentException e) {
			logWriter.log("x::RegData::getdbURI::IllegalArgumentException::"+e.getMessage());
			e.printStackTrace();
		} catch (MalformedURIException e) {
			logWriter.log("x::RegData::getdbURI::MalformedURIException::"+e.getMessage());
			e.printStackTrace();
		}	
	}
    
	/**
	 * Method for creating a database, if one does not exist.
	 * Should not be called directly. Call openDatabase instead, which in turn calls this.
	 */
    private void createDatabase()
    {
    	logWriter.log("In RegData createDatabase method");
		try {
			// This is called in openDatabase() anyway so shouldnt need to be called here
			//if (null == dbURI) getdbURI();	// Ensure dbURI is usable
			
			// Check to see if the database already exists.
			// dbExist is initialised to false, so this will catch an error
			// when the device gets rebooted, and dbExist is reset to false,
			// even though the database was previously created.
			// Plus, the below code creates the tables. We don't want to do that twice!
			dbExist=DatabaseFactory.exists(dbURI); // Will store if DB exists, ie T or F

			if (!dbExist)
			{
				logWriter.log("createDatabase::Checking for SD card...");
				sdCardPresent = Tools.hasSDCard();
				if (sdCardPresent)
				{
					logWriter.log("createDatabase::SD card present");
					logWriter.log("Creating a database at: "+dbURI.toString());
					storeDB = DatabaseFactory.create(dbURI);  //Create file
					logWriter.log("Database created");
					dbExist = DatabaseFactory.exists(dbURI); // Will store if DB exists, ie T or F
					
					// Now create the tables
					Statement st = storeDB.createStatement( DATABASE_CREATE );  //Populate tables
					// if "st" is unsuccessful, one of the "catch" blocks will
					//trigger before openDatabase is called.
					st.prepare();
					st.execute();  // Execute SQL
					st.close();
					
					// Now add the first values for REGISTRATION
					Statement st2 = storeDB.createStatement("INSERT INTO "+DATABASE_TABLE+" VALUES(0,\"0\")");
					st2.prepare();
					st2.execute();
					st2.close();
					storeDB.close(); storeDB=null;
					
				} // end if(sdCardPresent)
			} // end if(!dbExist)
			else
			{
				logWriter.log("DB already exists!");
			}
		}  // end try
		catch (DatabaseIOException e) {
			logWriter.log("x::RegData::createDatabase::DatabaseIOException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabasePathException e) {
			logWriter.log("x::RegData::createDatabase::DatabasePathException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabaseException e) {
			logWriter.log("x::RegData::createDatabase::DatabaseException::"+e.getMessage());
			e.printStackTrace();
		}
    } // end createDatabase()

    /**
     * Method to open the database. It also calls createDatabase if no DB exists
     * 
     */
    public boolean openDatabase()
    {
    	logWriter.log("In RegData openDatabase method");
		// Ensure URI is valid
		if (null == dbURI)
			getdbURI();

		// and make sure the DB exists. SHOULD always exist here, but just to be safe...
		if (!dbExist)
			createDatabase();

		try
		{
			storeDB=null;	// Set the DB to null so we know for definite its not already open
			storeDB = DatabaseFactory.open(dbURI);
			
			/*
			if (null != storeDB)
			{
				logWriter.log("RegData::openDatabase::The DB is already open!");
			}
			else
			{
				storeDB = DatabaseFactory.open(dbURI);
			}*/
			
			// From now on, we can check if the DB is open by comparing it to null	
			//logWriter.log("openDatabase::DB is"+(null==storeDB?"NULL!":"OPEN!"));
		} catch (ControlledAccessException e) {
			logWriter.log("x::RegData::openDatabase::ControlledAccessException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabaseIOException e) {
			logWriter.log("x::RegData::openDatabase::DatabaseIOException::"+e.getMessage());
			e.printStackTrace();
		} catch (DatabasePathException e) {
			logWriter.log("x::RegData::openDatabase::DatabasePathException::"+e.getMessage());
			e.printStackTrace();
		}
		return (null==storeDB?false:true);
    } // end openDatabase
    
/**
 * This method retrieves the current registration stage value.
 * @return current registration value.
 */
    public int getStageValue()
    {
    	return currentState;
    }
    /**
     * This method retrieves the current registration stage value from the local database for using within the class.
     * @return current registration value.
     */
    private int getStageValueDB()//KEY_STAGE
    {
    	//Cursor cursor = getStoreDBoutput();
    	/*int temp =  cursor.getInt(0);
    	cursor.close();
    	if(cursor.isClosed()) 
    	{	logger.log("==Cursor is closed..."); }*/

    	return queryINTfirst(DATABASE_TABLE, null, null, null, null, null, null);	//getStoreDBoutput().getInt(0);
    }
    /**
     * This method sets the current registration stage value in the local database.
     * 
     * @param The input value is the current registration stage value.
     */
    public void setStageValue(int inputVal)//KEY_STAGE
    {
    	logWriter.log("Registration::setStageValue:;Updating stage in database");
    	currentState = inputVal;//set for quick local access
    	//ContentValues initialValues = new ContentValues();
        //initialValues.put(KEY_STAGE, inputVal);
        update(KEY_STAGE,String.valueOf(inputVal));//(DATABASE_TABLE, initialValues, null, null);
        
    }
    /**
     * This method retrieves the regID from the local database.
     * @return regID.
     */
    public synchronized String getRegSN()//KEY_NUMBER
    {
    	// This signature is taken from the Android one, which uses some of the extra parameters.
    	// We dont need them for blackberry though.
    	return querySTRINGfirst(DATABASE_TABLE, null, null, null, null, null, null);
    }
    
    /**
     * This method sets the regID in the local database.
     * 
     * @param The input value is the regID.
     */
    public void setRegSN(String inputVal)//KEY_NUMBER
    {
    	update(KEY_NUMBER,inputVal);
    	//regID=inputVal;
    }
    
		/**
		 * 
		 * This method updates rows in the database. It opens the database to write/update and then closes it.
		 * @param table a table to update in
		 * @param values a map from column names to new column values. null is a valid value that will be translated to NULL.
		 * @param whereClause the optional WHERE clause to apply when updating. Passing null will update all rows.
		 * @param whereArgs
		 */
		public void update(String rowToUpdate, String theValue)//(String table, ContentValues values, String whereClause, String[] whereArgs) 
		{
			// Update table
			// The Strings "whereClause" and "whereArgs[]" are null
			
			logWriter.log("In RegData update");
			
			//Ensure a connection to the database exists
            if (null == storeDB)
            	openDatabase(); 
            
			try
	        {           
				String sqlUpdate = "UPDATE "+DATABASE_TABLE+" SET "+rowToUpdate+"="+theValue;// WHERE "+KEY_NUMBER+"=?";
	            Statement st = storeDB.createStatement(sqlUpdate);
	            st.prepare();
                st.execute();
                st.reset();
	            st.close();
	            // Also, we might want to set the value of the variable to the new value
	            if (KEY_NUMBER == rowToUpdate)
	            	Registration.regID=theValue;	
	            else //if (KEY_STAGE == rowToUpdate)
	            	currentState=Integer.parseInt(theValue);
	            storeDB.close(); storeDB=null;
	            	
	        }
	        catch ( DatabaseException e )
	        {
	        	logWriter.log("x::RegData::update::DatabaseException::"+e.getMessage());
	            //System.out.println( e.getMessage() );
	            e.printStackTrace();
	        }

		}	// end update()
		
		/**
		 * This method query the given table, returns the value of the requested column as an int. 
		 * It opens the database to read/query and then closes it.
		 * @param table The table name to compile the query against.
		 * @param columns A list of which columns to return.
		 * @param selection A filter declaring which rows to return.
		 * @param selectionArgs String array of rows to return.
		 * @param groupBy A filter declaring how to group rows.
		 * @param having HAVING clause 
		 * @param orderBy used how to order the rows.
		 * @return Column value as an int.
		 */
		public int queryINTfirst(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) 
		{
			logWriter.log("In RegData queryINTfirst");
			// SELECT colums FROM table WHERE
			int column_value=0;
			
			//Ensure a connection to the database exists
            if (null == storeDB)
            	openDatabase(); 
			
			try
			{	            
	            //TODO: Make proper SQL Statement
	            //Statement st = storeDB.createStatement("SELECT "+KEY_STAGE+" FROM "+DATABASE_TABLE);
				Statement st = storeDB.createStatement("SELECT * FROM "+DATABASE_TABLE);
				
	            st.prepare();
	            Cursor cursor = st.getCursor();
				
				cursor.first();
				Row row = cursor.getRow();
				// Here's the only line thats changed from querySTRINGfirst
				column_value = row.getInteger(0);
				st.close();
				cursor.close();
				storeDB.close(); storeDB=null;

			} catch (IllegalArgumentException e) {
				logWriter.log("x::RegData::queryINTfirst::IllegalArgumentException::"+e.getMessage());
				e.printStackTrace();
			} catch (DatabaseException e) {
				logWriter.log("x::RegData::queryINTfirst::DatabaseException::"+e.getMessage());
				e.printStackTrace();
			} catch (DataTypeException e) {
				logWriter.log("x::RegData::queryINTfirst::DataTypeException::"+e.getMessage());
				e.printStackTrace();
			} 
			return column_value;
		}
		
		/**
		 * This method query the given table, returns the value of the requested column as a String. It opens the database to read/query and then closes it.
		 * @param table The table name to compile the query against.
		 * @param columns A list of which columns to return.
		 * @param selection A filter declaring which rows to return.
		 * @param selectionArgs String array of rows to return.
		 * @param groupBy A filter declaring how to group rows.
		 * @param having HAVING clause 
		 * @param orderBy used how to order the rows.
		 * @return Column value as a String.
		 */
		public String querySTRINGfirst(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) 
		{
			logWriter.log("In RegData querySTRINGfirst");
			String column_value="";
			
			//Ensure a connection to the database exists
            if (null == storeDB)
            	openDatabase();
            
			try
			{	            
	            //Statement st = storeDB.createStatement("SELECT "+KEY_NUMBER+" FROM "+DATABASE_TABLE);
				Statement st = storeDB.createStatement("SELECT * FROM "+DATABASE_TABLE);
				
	            st.prepare();
	            Cursor cursor = st.getCursor();				
				cursor.first();
				
				Row row = cursor.getRow();
				// Here's the only line thats changed from querySTRINGfirst
				column_value = row.getString(1);
				st.close();
				cursor.close();
				storeDB.close(); storeDB=null;
			}
			catch (IllegalArgumentException e) {
				logWriter.log("x::RegData::querySTRINGfirst::IllegalArgumentException::"+e.getMessage());
				e.printStackTrace();
			} catch (DatabaseException e) {
				logWriter.log("x::RegData::querySTRINGfirst::DatabaseException::"+e.getMessage());
				e.printStackTrace();
			} catch (DataTypeException e) {
				logWriter.log("x::RegData::querySTRINGfirst::DataTypeException::"+e.getMessage());
				e.printStackTrace();
			} 
			return column_value;
		}	
}