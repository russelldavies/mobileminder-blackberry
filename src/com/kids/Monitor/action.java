package com.kids.Monitor;

import net.rim.device.api.util.Persistable;

/**
 * 
 * The action class is used to instantiate action objects to remain persistent in the devices storage.
 *
 */
public class action implements Persistable
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