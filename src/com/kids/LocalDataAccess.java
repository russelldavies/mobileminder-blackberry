package com.kids;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.Message;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.database.DatabaseIOException;
import net.rim.device.api.database.DatabasePathException;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;

public class LocalDataAccess
{
        private LocalDataAccess()
        {       }
        
        public static LocalDataReader getLocalDataAccessRef()
        {
        //get sdk version
        if (5 > Tools.getGenOSVersion()) 
                {
                return new innerLegacyDataAccess();
                //LegacyDataAccess actLog = legLog;
                } 
        else //if (5<=ver)
                {
                return new innerLocalDataAccess();
                //LocalDataAccess actLog = locLog;
                }
        }
}
/**
 * This class creates a table in the local database which stores all the information which need to be sent to the web server.
 */
class innerLocalDataAccess implements LocalDataReader//, LocalDataReader
{
    public static Debug logWriter = Logger.getInstance();
    
    //ANDROID
    //private static ArrayList<Message> otherMessages = new ArrayList<Message>(); 
    
    //Blackberry
    //public static Vector otherMessages = new Vector();
    
    public static final String  DATABASE_NAME    = "CVKf";
    public static final String  DATABASE_TABLE   = "LocalData";
   // private static final int     DATABASE_VERSION = 2;
    //The primary key MUST be named '_id' Android convention
    private static final String  KEY_INDEX       = "_id";//MAY OVER FLOW!!
    private static final String  KEY_TIME        = "time";
    private static final String  KEY_VALUE       = "value";
    
    //ANDROID
    // Should be same for Blackberry
    public static final String  DATABASE_CREATE  = "create table `"+DATABASE_TABLE+ "`("
                                                   					+"`"+KEY_INDEX  +"` integer NOT NULL primary key autoincrement,"
                                                   					+"`"+KEY_TIME   +"` TEXT    NOT NULL,"     
                                                   					+"`"+KEY_VALUE  +"` TEXT);";
    
    public static String   DATABASE_LOCATION = "file:///SDCard/Databases/MobileMinder/";
    public static Database storeDB			 = null;
    public static URI      dbURI			 = null;
    
    public static boolean sdCardPresent	= false;	// Bool to keep track of when SD Card is mounted
    public static boolean dbExist		= false;	// For checking to see if the DB already exists before each DB call
    
    /**
     * This is the constructor of LocalDataAccess. It creates the environment for the table in the local database used to store phone actions..
     * @param _context Interface to global environment that the current application is running in.
     */
        public innerLocalDataAccess(/*Context _context*/)
        {
        	logWriter.log("innerLocalDataAccess::constructor");
        	//String mc             = System.getProperty("fileconn.dir.memorycard");
            //String mcname         = System.getProperty("fileconn.dir.memorycard.name");
	
        	// This is never true in the simulator. Real phones might mount the SD card quicker,
        	// so this check is valid here, purely for those current/future faster devices.
	        if (Tools.hasSDCard())
	        {
	        	sdCardPresent=true;
	        	openDatabase();
	        }
	        else
	        {
	        	logWriter.log("No SD Card mounted...yet!");
	        }
        }  // end constructor

        /**
         * Method that sets the URI of the database, so it can be used with DatabaseFactory
         */
		private static void getdbURI()
		{
			logWriter.log("In getdbURI method");
			try {
				dbURI = URI.create(DATABASE_LOCATION+DATABASE_NAME);
				logWriter.log("getdbURI::URI"+(null==dbURI ? " is not " : " is ")+"usable");
			} catch (IllegalArgumentException e) {
				logWriter.log("x::LocalDataAccess::getdbURI::IllegalArgumentException::"+e.getMessage());
				e.printStackTrace();
			} catch (MalformedURIException e) {
				logWriter.log("x::LocalDataAccess::getdbURI::MalformedURIException::"+e.getMessage());
				e.printStackTrace();
			}	
		}
        
		/**
		 * Method for creating a database, if one does not exist.
		 * Should not be called directly. Call openDatabase instead, which in turn calls this.
		 */
        private static void createDatabase()
        {
        	logWriter.log("In createDatabase method");
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
						storeDB = DatabaseFactory.create(dbURI);  //Create file
						dbExist = DatabaseFactory.exists(dbURI); // Will store if DB exists, ie T or F
						
						// Now create the tables
						Statement st = storeDB.createStatement( DATABASE_CREATE );  //Populate tables
						// if "st" is unsuccessful, one of the "catch" blocks will
						//trigger before openDatabase is called.
						st.prepare();
						st.execute();  // Execute SQL
						st.close();
					}/*
					else
					{ // Schedules an app to (re)start in a specified number of milliseconds
					  // can be used if there is no SD card. Maybe try this from Driver?
					  //http://supportforums.blackberry.com/t5/Java-Development/how-to-restart-a-Background-running-Application/td-p/335449/page/2
						ApplicationManager.getApplicationManager().scheduleApplication(thisApp, System.currentTimeMillis() + 60100, true);
					}*/

				}

			} catch (DatabaseIOException e) {
				logWriter.log("x::LocalDataAccess::createDatabase::DatabaseIOException::"+e.getMessage());
				e.printStackTrace();
			} catch (DatabasePathException e) {
				logWriter.log("x::LocalDataAccess::createDatabase::DatabasePathException::"+e.getMessage());
				e.printStackTrace();
			} catch (DatabaseException e) {
				logWriter.log("x::LocalDataAccess::createDatabase::DatabaseException::"+e.getMessage());
				e.printStackTrace();
			}		
        }

        /**
         * Method to open the database. It also calls createDatabase if no DB exists
         * 
         */
        public static boolean openDatabase()
        {
        	logWriter.log("In openDatabase method");
			try {
				// Ensure URI is valid
				if (null == dbURI)
					getdbURI();

				// and make sure the DB exists. SHOULD always exist here, but just to be safe...
				if (!dbExist)
					createDatabase();
				
				if (null != storeDB)
				{
					logWriter.log("LocalDataAccess::openDatabase::The DB is already open!");
				}
				else 	// Checked here, but also checked before call. Overkill??
				{
					storeDB = DatabaseFactory.open(dbURI);
				}
				
				// From now on, we can check if the DB is open by comparing it to null	
				//logWriter.log("openDatabase::DB is"+(null==storeDB?"NULL!":"OPEN!"));
			} catch (ControlledAccessException e) {
				logWriter.log("x::LocalDataAccess::openDatabase::ControlledAccessException::"+e.getMessage());
				e.printStackTrace();
			} catch (DatabaseIOException e) {
				logWriter.log("x::LocalDataAccess::openDatabase::DatabaseIOException::"+e.getMessage());
				e.printStackTrace();
			} catch (DatabasePathException e) {
				logWriter.log("x::LocalDataAccess::openDatabase::DatabasePathException::"+e.getMessage());
				e.printStackTrace();
			}
			return (null==storeDB?false:true);
        } // end openDatabase
        
		/**
         * This method adds the phone actions messages into the table used to store phone actions.
         * @param _message The phone actions from the monitor classes.
         */
        //@Override
        public synchronized void addMessage(Message _message) 
        {
        	addValue(_message.getREST());        
        }
        
        /**
         * This method adds the information to the table used to store phone actions.
         * @param        _value the messages received from addMessage method.
         */
        private synchronized void addValue(String _value)
        {
	    	logWriter.log("innerLocalDataAccess::addValue");	    	
            logWriter.log("REST String: "+_value);
                    
            Date theDate = Calendar.getInstance().getTime();
            String dateTime = String.valueOf(theDate.getTime());
            
        	//Build SQL statement
        	StringBuffer sqlInsert = new StringBuffer();
        	sqlInsert.append("INSERT INTO ");
        	sqlInsert.append(DATABASE_TABLE);
        	sqlInsert.append("(");
        	sqlInsert.append(KEY_TIME);
        	sqlInsert.append(",");
        	sqlInsert.append(KEY_VALUE);
        	sqlInsert.append(") VALUES (");
        	sqlInsert.append("\"");
        	sqlInsert.append(dateTime);
        	sqlInsert.append("\",\"");
        	sqlInsert.append(_value);
        	sqlInsert.append("\")");
        	 
            
            Statement st;
            try {
            	logWriter.log("addValue::Try INSERT");

            	// Check if database is open. storeDB=null should mean it hasnt been opened yet
            	if (null == storeDB )
            	{
            		logWriter.log("localDataAccess::addValue::Calling openDatabase()...");
            		// NOTE: openDatabase method call is embedded in output
            		logWriter.log("localDataAccess::addValue::storeDB is"+(openDatabase()?" ":" NOT ")+"open");
            		logWriter.log("Trying to open DB again...");
            		logWriter.log("localDataAccess::addValue::storeDB is"+(openDatabase()?" ":" NOT ")+"open");
            	}           	            	
            	// storeDB should, hopefully, never be NULL at this point, as it should be opened
            	
                st = storeDB.createStatement(sqlInsert.toString());
                st.prepare();
                st.execute();
                st.close();  
                storeDB.close(); storeDB=null;
            } catch (DatabaseException e) {
            	logWriter.log("x::LocalDataAccess::addValue::DatabaseException:"+e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
            	logWriter.log("x::LocalDataAccess::addValue::IllegalArgumentException:URI:"+e.getMessage());
				e.printStackTrace();
			}

        }
        /**
         * This method retrieves first row's value in the table used to store phone actions.
         * @return a string contains all the information from row one in the table used to store phone actions.
         */
        public String getFirst()
        {
        	String result=null;
            result = getValue(0);
            return result;
        }
        
        /**
         * This method retrieves the specified row's REST String value in the table used to store device actions.
         * @param _index of the specified row in the table used to store device actions.
         * @return a string contains all the values from the specified index of the database.
         */
        public String getValue(int _index)
        {
            Row row;
            String value = "";
            // Get cursor pointing to first item in DB
            Cursor result = selectAllFromDB();
            
            try 
            {//TODO: cleanup
                result.position(_index);
                row=result.getRow();
                // String 2 in the DB is the REST string, eg (0:ID, 1:Time, 2:REST)
                value = row.getString(2);
                result.close();
            } 
            catch (DatabaseException e) 
            {
            	logWriter.log("x::innerLocalDataAccess::getValue::DatabaseEception::"+e.getMessage());
                e.printStackTrace();
            }
            catch (DataTypeException e) 
            {                	
            	logWriter.log("x::innerLocalDataAccess::getValue::DatabaseEception::"+e.getMessage());
                e.printStackTrace();
            }
            finally
            {   result = null;   }
            
            return value;
        }
        
        /**
         * This method removes the first row's value in the table used to store phone actions.
         * @throws  
         */
        public void removeFirst()
        {
                Row row;
                Cursor result = null;;
                try {
                        result = selectAllFromDB();
                        result.position(0);
                        //move to First
                        row=result.getRow();
                        removeValue(row.getInteger(0));//Get ID and remove. row(0) is unique ID in DB
                        result.close();
                } catch (DatabaseException e) {
                        logWriter.log("x::LocalDataAccess::removeFirst::DatabaseException:"+e.getMessage());
                        e.printStackTrace();
                } catch (DataTypeException e) {
                        logWriter.log("x::LocalDataAccess::removeFirst::DataTypeException:"+e.getMessage());
                        e.printStackTrace();
                }
                result = null;
        }
        
        /**
         * This method removes the information at specified position of the table used to store phone actions.
         * @param _index The position where the information is needed to be removed.
         */
        public synchronized void removeValue(int _index)
        {
        	//Build SQL statement
        	StringBuffer sqlCreate = new StringBuffer();
        	sqlCreate.append("DELETE FROM ");
        	sqlCreate.append(DATABASE_TABLE);
        	sqlCreate.append(" WHERE ");
        	sqlCreate.append(KEY_INDEX);
        	sqlCreate.append("=");
        	sqlCreate.append(String.valueOf(_index));
        	
        	try {
        		if (null == storeDB)
        			openDatabase();
                Statement st = storeDB.createStatement(sqlCreate.toString());
                st.prepare();
                st.execute();
                st.close();
                storeDB.close(); storeDB=null;
            } catch (ControlledAccessException e) {
                logWriter.log("x::LocalDataAccess::removeValue::ControlledAccessException:"+e.getMessage());
                e.printStackTrace();
            } catch (DatabaseIOException e) {
                logWriter.log("x::LocalDataAccess::removeValue::DatabaseIOException:"+e.getMessage());
                e.printStackTrace();
            } catch (DatabasePathException e) {
                logWriter.log("x::LocalDataAccess::removeValue::DatabasePathException:"+e.getMessage());
                e.printStackTrace();
            } catch (DatabaseException e) {
                logWriter.log("x::LocalDataAccess::removeValue::DatabaseException:"+e.getMessage());
                e.printStackTrace();
            }
        }	// end removeValue
        
        /**
         * This method gets the length of a certain Cursor.
         * @return The length of the current cursor object.
         */
        public int length()
        {       
            Cursor result = selectAllFromDB();
            int size=0;
            try {
                    //result.last returns FALSE is its empty (result=null if cant be opened)
                    // Otherwise, it moves to the last position
            		logWriter.log("if result.last");
            		 // TODO: Causing NullPointerException when DB cant be opened
                    if( null != result && result.last())
                    {   // Assuming its not empty, getPosition will return an INT, else a null
                    	// but it wouldnt be in here if it was empty
                    	logWriter.log("inside result.last. setting size...");
                        size = result.getPosition();
                        logWriter.log("Closing result");
                        result.close();
                        storeDB.close(); storeDB=null;
                    }
                    else
                    {
                    	logWriter.log("Cursor result cannot be opened!");
                    }
            } catch (DatabaseException e) {
                    logWriter.log("x::LocalDataAccess::length::"+e.getMessage());
                    e.printStackTrace();
            }

            result = null;
            //Size SHOULD now have either 0, or the INT value of Cursor.last()
            return size;
        }
        
        /**
         * This method retrieves the information in the local database.
         * @return a cursor object which contains all the information of the table used to store phone actions.
         */
        private Cursor selectAllFromDB()
        {
			logWriter.log("innerLocalDataAccess::getStoreDBouput");
        	Statement st;
            Cursor c=null;
            
            //Ensure a connection to the database exists
            if (null == storeDB)
            	openDatabase();            
            
                try {
					st = storeDB.createStatement("SELECT * FROM "+DATABASE_TABLE);
	                st.prepare();
	                st.execute();
	                c = st.getCursor();
	                //st.close(); //TODO: We're returning a cursor to the DB. Do we need to close the statement?
				} catch (DatabaseException e) {
					logWriter.log("x::innerLocalDataAccess::getStoreDBouput::DatabaseException::"+e.getMessage());
					e.printStackTrace();
				}

			// Return CURSOR or NULL
            return c;
        }
}
        
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////


        /**
         * This class is used to create the local database.
         *
         */
class DatabaseHelper //extends SQLiteOpenHelper 
{
        /**
         * This is the default constructor that is needed when implementing the SOLiteOpenHelper.
         * 
         * @param _context Interface to global environment that the current application is running in.
         */
        DatabaseHelper(/*Context _context*/) 
        {
                //super(/*_context,*/ LocalDataAccess.DATABASE_NAME, null, 2);//DATABASE_VERSION);
                super();
        }
        
        /**
         * This method creates the database calls setupFirstEntry to initialise the local storage.
         * @param NONE
         */
        public void onCreate(/*Database _db*/) 
        {
             try
             {
            	//innerLocalDataAccess.dbURI = URI.create(innerLocalDataAccess.DATABASE_LOCATION+innerLocalDataAccess.DATABASE_NAME);
            	innerLocalDataAccess.logWriter.log("DataBaseHelper create...");
            	innerLocalDataAccess.logWriter.log("DatabaseHelper::dbURI is: "+innerLocalDataAccess.dbURI.toString());
                //innerLocalDataAccess.storeDB = DatabaseFactory.open(innerLocalDataAccess.dbURI);
                if (null == innerLocalDataAccess.storeDB)
                	innerLocalDataAccess.openDatabase();
            	Statement st = innerLocalDataAccess.storeDB.createStatement(innerLocalDataAccess.DATABASE_CREATE);
                
                st.prepare();
                st.execute();
                st.close();
                innerLocalDataAccess.storeDB.close(); innerLocalDataAccess.storeDB = null;
            }
            catch ( Exception e ) 
            {         
                    innerLocalDataAccess.logWriter.log("x::LocalDataAccess::DBHelper::onCreate::"+e.getMessage());
                e.printStackTrace();
            }
            
            //ANDROID
            //_db.execSQL(LocalDataAccess.DATABASE_CREATE);
        }
        
        /**
         * This method initialises the local storage.
         * @param _db SQLiteDatabase
         */
        public void onUpgrade(/*Database _db, int oldVersion, int _newVersion*/) 
        {
             try
            { 
            	 innerLocalDataAccess.logWriter.log("DBHelper::onUpgrade");
            	 //This should be opened at the beginning and kept open
                //innerLocalDataAccess.storeDB = DatabaseFactory.open(innerLocalDataAccess.dbURI);
                Statement st = innerLocalDataAccess.storeDB.createStatement("DROP IF TABLE EXISTS "
                                                                             +innerLocalDataAccess.DATABASE_TABLE);
    
                st.prepare();
                st.execute();
                st.close();
                innerLocalDataAccess.storeDB.close(); innerLocalDataAccess.storeDB = null;
            }
            catch ( Exception e ) 
            {
            	innerLocalDataAccess.logWriter.log("x::LocalDataAccess::DBHelper::onUpgrade::"+e.getMessage());
                e.printStackTrace();
            }
                        
        }
        
        public static void addOtherMessage(Message otherMessages)
        {
                //LocalDataAccess.otherMessages.addElement(otherMessages);//.add(otherMessages);
        }
}



//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////



        /**
         * 
         * LocalDataAccess is used by the Listener objects to store triggered events that occur in the system.
         *
         */
class innerLegacyDataAccess implements LocalDataReader
{       
        private PersistentObject store;//HD
        private Vector LocalData;      //list of Actions
        Debug logWriter = Logger.getInstance();
        
        /**
         * 
         * The constructor calls the setupStore method
         *
         */
                
        public innerLegacyDataAccess()
        {
                setupStore();
        }
        
        /**
         * 
         * This function creates a persistent storage object which hold a Vector to store triggered actions on the device.
         * 
         */
                
        private void setupStore()
        {
                store = PersistentStore.getPersistentObject(0xdec6a67096f833cL);
                
                synchronized(store)
                {
                        if(null == store.getContents()) 
                        {
                                store.setContents(new Vector());
                                store.commit();
                        }
                }
                
                LocalData = new Vector();
                LocalData = (Vector)store.getContents();
        }
                
        /**
         * 
         * This function returns the current date and time in a formatted string of 12 digits ending with the seconds.
         * <p><b>Example:</b>
         * An example of this is 31st of August 2010 would be represented as 100831105221
         * 
         * @return
         * String containing formatted date
         * 
         */
                
        public String getDate()
        {
                //return new SimpleDateFormat("HH:mm:ss dd-MM-yy").format(new Date());
                return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }

        //TODO addAction function should return a boolean to ensure action has been stored
        /**
         * 
         * This function adds an action to the persistent storage on the device.
         * @param  inputType type of action
         * @param  inputStatus status of the action
         */
        public void addAction(int inputType, String inputStatus)
        {
                addAction(false, inputType,inputStatus);
        }

        /**
         * 
         * This function adds an action to the persistent storage on the device.
         * @param  inputError error status
         * @param  inputType type of action
         * @param  inputStatus status of the action
         */
        public void addAction(boolean inputError, int inputType, String inputStatus)
        {
                addAction(inputError, inputType,inputStatus,"");
        }

        /**
         * This function adds an action to the persistent storage on the device.
         * @param  inputType type of action
         * @param  inputStatus status of the action
         * @param  inputDestinationAddress action address
         */
        public void addAction(int  inputType, 
                                                  String inputStatus,
                                                  String inputDestinationAddress)
        {
                addAction(false,inputType,inputStatus,inputDestinationAddress);
        }
                
        /**
         * This function adds an action to the persistent storage on the device.
         * @param  error error status
         * @param  inputType type of action
         * @param  inputStatus status of the action
         * @param  inputDestinationAddress action address
         */
        public void addAction(boolean error,
                              int    inputType, 
                              String  inputStatus,
                              String  inputDestinationAddress)
        {
                logWriter.log("LegacyDataAccess::----------------"+inputDestinationAddress);
        /*      
                synchronized(Application.getEventLock()){    UiEngine ui = Ui.getUiEngine();
        Screen screen = new Dialog(Dialog.D_OK, "PantaLOOONS!!!!!!",
            Dialog.OK,           Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),         Manager.VERTICAL_SCROLL);
        ui.pushGlobalScreen(screen, 1, UiEngine.GLOBAL_QUEUE);
    }*/
                
                synchronized(store) 
                {
                        LocalData.addElement(new action(error,
                                                        inputType, 
                                                        getDate(), 
                                                        inputStatus,
                                                        inputDestinationAddress
                                                        )
                        					);
                        store.setContents(LocalData);
                        store.commit();
                }
        }
                
        /**
         * Retrieves an action from the persistent storage based on an index
         * @param index action identifier
         * @return Action object.
         */
        public action getAction(int index)
        {
                synchronized (store)
                {
                        LocalData = (Vector)store.getContents();
                        
                        if (!LocalData.isEmpty()) 
                        {
                                logWriter.log("LocalDataAccess::LocalData is not Empty");
                                return (action)LocalData.elementAt(index);
                        }
                        else
                        {
                                logWriter.log("LocalDataAccess::LocalData is Empty");
                                return new action();
                        }
                }
        }
                
        /**
        * Removes an action from the persistent storage based on an index
        * @param index action identifier
        * @return boolean true if the action has been removed
        */
                        
        public boolean removeAction(int index)
        {
                synchronized (store)
                {
                        LocalData = (Vector)store.getContents();
                        
                        if (!LocalData.isEmpty()) 
                        {
                                if(LocalData.size() > index)//test if the index is there.
                                {
                                        LocalData.removeElementAt(index);
                                        return true;
                                }
                                else
                                {return false;}
                        }
                        else
                        {return false;}
                }
        }

        /**
         * Retrieves the size of the object
         * @return integer size of the object
         */
        public int length()
        {
                synchronized (store)
                {
                        LocalData = (Vector)store.getContents();
                        return LocalData.size();
                }
        }

        public void addMessage(Message inputMessage) {
                // TODO Auto-generated method stub
                // Adds data to vector
                LocalData.addElement(inputMessage.getREST());
        }

        public String getFirst() {
                // TODO Auto-generated method stub
                return null;
        }

        public String getValue(int _index) {
                // TODO Auto-generated method stub
                return null;
        }

        public void removeFirst() {
                // TODO Auto-generated method stub
                
        }

        public void removeValue(int _index) {
                // TODO Auto-generated method stub
                
        }
}

        /**
         * 
         * The action class is used to instantiate action objects to remain persistent in the devices storage.
         *
         */
class action implements Persistable
{ 
        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_IDEL    = 1;
        public static final int TYPE_CALL    = 2;
        public static final int TYPE_TEXT    = 3;
        public static final int TYPE_MAIL    = 4;
        public static final int TYPE_WEB     = 5;
        public static final int TYPE_APP     = 6;
        public static final int TYPE_GPS     = 7;
        public static final int TYPE_SERVER  = 8;
        
        public static final String Outgoing = "Outgoing";
        public static final String Incoming = "Incoming";
        
        private int     type;
        private boolean error;
        private String  timeStamp;
        private String  destinationAddress;
        private String  status;

        /**
         * Creates a default action object when no parameters are provided
         */
        public action()
        {
        	this(false,action.TYPE_UNKNOWN,"","","");
        }

        /**
         * Creates an action object with with a integer error input and stores it as a boolean
         * 
         * @param  inputError error status
         * @param  inputType action type
         * @param  inputTimeStamp time of action occurrence
         * @param  inputStatus action details
         * @param  inputDestinationAddress action address
         */
        public action(int    inputError,
                      int    inputType, 
                      String inputTimeStamp, 
                      String inputStatus,
                      String inputDestinationAddress)
        {
                this(((0==inputError)?Boolean.FALSE:Boolean.TRUE).booleanValue(),
                                inputType,
                                inputTimeStamp,
                                inputStatus,
                                inputDestinationAddress);
        }

        /**
        * Creates an action object with with a boolean error input
        * 
        * @param  inputError error status
        * @param  inputType action type
        * @param  inputTimeStamp time of action occurrence
        * @param  inputStatus action details
        * @param  inputDestinationAddress action address
        */
        public action(boolean inputError,
                                  int    inputType, 
                                  String  inputTimeStamp, 
                                  String  inputStatus,
                                  String  inputDestinationAddress)
        {
                error                      = inputError;
                type                       = inputType;
                timeStamp                  = inputTimeStamp;
                status                     = inputStatus;
                destinationAddress 		   = inputDestinationAddress;
        }
                
        /**
         * retrieves error status of an action
         * 
         * @return error value
         */
        public boolean getIsError()
        {
        	return error;
        }

        /**
         * retrieves the type of an action
         * 
         * @return type value
         */
        public int getType()
        {
        	return type;
        }
                
        /**
         * retrieves the action address
         * 
         * @return action address
         */
        public String getDestinationAddress()
        {
        	return new String(destinationAddress);
        }

        /**
         * retrieves the time of the action
         * 
         * @return time of action
         */
        public String getTimeStamp()
        {
        	return new String(timeStamp);
        }

        /**
         * retrieves the details about the action
         * 
         * @return action details
         */
        public String getStatus()
        {
        	return new String(status);
        }

        /**
         * retrieves the name of an action type based on a type number
         * 
         * @param  inputType number representing a type
         * @return name of the corresponding type
         */
        public static String findType(int inputType)
        {
                String textVal = new String();
                switch(inputType) 
                {
                    //case -3: textVal = "System Error"; break;
                    //case -2: textVal = "Logic Error"; break;
                    
                    case 0: textVal = "Unknown"; break;
                    case 1: textVal = "Idel";    break;
                    case 2: textVal = "Call";    break;
                    case 3: textVal = "Text";    break;
                    case 4: textVal = "Mail";    break;
                    case 5: textVal = "Web";     break;
                    case 6: textVal = "App";     break;
                    case 7: textVal = "GPS";     break;
                    case 8: textVal = "Server";  break;
                }
                
                return textVal;
        }

        /**
         * This function overrides the toString method inherent to all object.
         * It facilitates the pursuit of more detailed information about the action object.
         * 
         *@return String containing the error status, time of action and action type.
         */
        public String toString()
        {
            String textVal = findType(type);
            
            if(error)
            {
            	return "!> "+timeStamp + " - " + textVal;
            }
            else
            {
            	return timeStamp + " - " + textVal;
            }
        }
}    // end class Action