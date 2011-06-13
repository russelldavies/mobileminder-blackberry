package com.spotlight.track;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;

/*
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.Message;
*/
/**
 * 
 * MyServerUpload establishes the connection to the server
 * and facilitates in the routine delivery of triggered event data to the server
 * 
 */

public class MyServerUpload extends Thread
{
	LocalDataReader actLog = LocalDataAccess.getLocalDataAccessRef();
	//private LocalDataReader actLog;
	private int             sendToServerTime;
	private int 			deviceID;
	private int				employerID;
	Debug log = Logger.getInstance();
/**
 * Initialises the ServerUpload thread with the appropriate parameters.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputEmployerID user identifier
 * @param inputDeviceID device identifier
 * @param inputUploadTimer interval value
 */
	public MyServerUpload(LocalDataReader inputAccess,
						  int			  inputEmployerID,
						  int 			  inputDeviceID,
						  int			  inputUploadTimer)
	{
		actLog           = inputAccess;
		employerID 		 = inputEmployerID;
		deviceID 		 = inputDeviceID;
		sendToServerTime = inputUploadTimer;
		log.log("MyServerUpload begin...");
		this.start();
	}
	
/** 
 * 
 * 		Sends all device actions to the server within specific intervals
 * 
 * <p>
 *  	This function is called when the constructor starts, it stores the actions to the action log.
 *  	It sleeps for a certain interval, then sends a batch to the server of all the actions during the interval.
 *  	Each action is loaded, sent and then removed and the same process is used for the next action.
 *  	If an error occurs a error message is logged in the action log.
 * 	
 */
	
	public void run()
	{/*
		log.log("In MyServerUpload->run()");
		actLog.addMessage(action.TYPE_SERVER,"Starting: MyServerUpload");
		
		try
		{
			SLDBserviceSoap_Stub server = new SLDBserviceSoap_Stub();
			
			while(true)
			{
				this.sleep(sendToServerTime);
				log.log("Length of actLog="+actLog.length());
				
				while(0 < actLog.length())	//Dont send Start Upload
				{
					action anAction = actLog.getAction(0);
					String result = server.addAction(
										employerID,
										deviceID,
										anAction.getIsError(),
										anAction.getType(),
										anAction.getTimeStamp(),
										anAction.getStatus(),
										anAction.getDestinationAddress());
					
					
					log.log("MyServerUpload::Server.addAction DONE");
					log.log("Result="+result);
					if("ok".equals(result))
					{
						actLog.removeAction(0);//remove the First Action
						log.log(""+result);
					}
					else
					{
						log.log("Not equal OK");
						actLog.addMessage(true, action.TYPE_SERVER,"Error uploading Action to server: "+result);//.replace('\n', ' '));
						log.log(""+result);
						break;//jump out of loop!
					}
				}
				System.out.println("MyServerUpload::Out of while(true)");
			}
		} 
		catch(InterruptedException e)
			{
			log.log("InterruptException in MyServerUpload");
			actLog.addMessage(true,action.TYPE_SERVER,e.toString());
			}
		catch(Exception e)
			{
			log.log("Exception in MyServerUpload");
			actLog.addMessage(true,action.TYPE_SERVER,e.toString());
			}
  */  }  // end of Run()
}

