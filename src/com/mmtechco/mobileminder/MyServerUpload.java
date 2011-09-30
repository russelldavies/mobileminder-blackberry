package com.mmtechco.mobileminder;

/**
 * 
 * MyServerUpload establishes the connection to the server and facilitates in the routine delivery of triggered event data to the server
 * 
 */

public class MyServerUpload extends Thread
{
	private LocalDataAccess actLog;
	private int             sendToServerTime;
	private int 			deviceID;
	private int				employerID;
	
/**
 * Initialises the ServerUpload thread with the appropriate parameters.
 * Calls the start() method on the thread which invokes the run() method of the thread.
 * 
 * @param inputAccess log of actions
 * @param inputEmployerID user identifier
 * @param inputDeviceID device identifier
 * @param inputUploadTimer interval value
 */
	public MyServerUpload(LocalDataAccess inputAccess,
						  int			  inputEmployerID,
						  int 			  inputDeviceID,
						  int			  inputUploadTimer)
	{
		actLog           = inputAccess;
		employerID 		 = inputEmployerID;
		deviceID 		 = inputDeviceID;
		sendToServerTime = inputUploadTimer;
				
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
	{
		actLog.addAction(action.TYPE_SERVER,"Starting: MyServerUpload");
		/*
		try
		{
			Server server = new Server();
			
			while(true)
			{
				Thread.sleep(sendToServerTime);

				while(0 != actLog.length())//Dont send Start Upload
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
	
					if("ok".equals(result))
					{
						actLog.removeAction(0);//remove the First Action
						System.err.println(result);
					}
					else
					{
						actLog.addAction(true, action.TYPE_SERVER,"Error uploading Action to server: "+result);//.replace('\n', ' '));
						System.err.println(result);
						break;//jump out of loop!
					}
				}

			}
		}
		catch(InterruptedException e)
		{actLog.addAction(true,action.TYPE_SERVER,e.toString());}
		catch(Exception e)
		{actLog.addAction(true,action.TYPE_SERVER,e.toString());}*/
    }
}

