package com.kids;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.util.CRC16;
import net.rim.device.api.util.CRC24;
import net.rim.device.api.util.CRC32;

import com.kids.Logger;
import com.kids.net.Reply;
import com.kids.net.Security;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
import com.kids.prototypes.MMServer;
import com.kids.prototypes.MMTools;
import com.kids.prototypes.Message;

/*
interface Message
{
	String 	getType();//call,text..
	boolean getError();
	String 	getTime();
	String 	getInfo();
	String 	getREST();//calls this.toString()
}
*/
/**
 * This class monitors for new actions stored in the local storage for recording actions and sends them to the web server at specific intervals.
 */
public class Server extends Thread implements MMServer
{

	private Debug logger = Logger.getInstance();
	private MMTools tools = Tools.getInstance();

	/*
	private final String ServerFeedBack_good = "ok";
	private final String ServerFeedBack_Stop = "no";
	private final String ServerFeedBack_Bad  = "bad";
	*/
	
	//private final int		HeartBeat = 99;
	private LocalDataReader actLog;
	private final String 	URL;
//	private String 	 		deviceId;
	private int 		 	freq = 1000 * 15; //freq = 1000 * 60 * 5; //5 min
	private boolean			live;
	private String 			serverErrorReply;
	private Random 			generator;
	private Security 		security;
	private final String 	charSET = "!$&()*+-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]_abcdefghijklmnopqrstuvwxyz~";  
//	private int crc;
	//create errormessage object

	/**
	 * The constructor initialises the server parameters, creates a new security instance and starts the server connection
	 * 
	 * @param inputActLog local storage location
	 */
	public Server(LocalDataReader inputActLog)
	{		
		logger.log("Starting.. Server");
		security = new Security();
		//URL = Controller.getString(R.string.URL_webservice);
		//logger.log(URL);
		//URL		= "http://www.associatemobile.com/mobileminder/WebService.php?";
		//Development Server
		URL 	 = "http://217.115.115.148:8000/dev1/mobileminder.net/WebService.php?";
		//URL = "http://192.168.0.20/mobileminder.net/WebService.php?";
	//	URL = "http://192.168.1.41/mobileminder.net/WebService.php?";
		//URL 	 = "http://192.168.81.1/kids/webservice.php?";
//		deviceId = "xxxxx";
		serverErrorReply =  Tools.RestElementSeparator+
							Tools.RestElementSeparator+//reply
		 				 1 +Tools.RestElementSeparator+//error
		 				 	Tools.RestElementSeparator;//CallingCODE
		//	Controller.getString(R.string.Error_ServerTimeOut);
				

		actLog 	 = inputActLog;
		live = true;
		generator = new Random();
		
		//startup();
		logger.log("Started Server");
	}
	
	/**
	 * This method monitors the local storage for new messages stored at specific intervals and sends them to the server.
	 *  
	 */
	public void run()
	{		
		logger.log("Server running");
		String[] resultREST;
		/*		for(int count = 0; count <characters.length(); count++)
		{
			contactRESTServer(""+characters.charAt(count));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		int counter = -1;
		
		while(true)
		{
			logger.log("Checking for new messages to send "+actLog.length());
			logger.log("contactServer..");
			
			/*if(actLog.length() == 0)
			{
				resultREST = contactRESTServer(Registration.getRegID()+RestElementSeparator+HeartBeat).split(RestElementSeparator);
				logger.log(resultREST[4]);
			}
			else*/
			//{
				while(0 < actLog.length())//checks if a message is in the local storage
				{
					//sends message to server and receives a reply. 
					// NO String.split() IN J2ME - http://stackoverflow.com/questions/657627/split-string-logic-in-j2me
					//resultREST = contactServer(actLog.getFirst()).getREST().split(Tools.RestElementSeparator);
					resultREST = Tools.split(contactServer(actLog.getFirst()).getREST(), Tools.RestElementSeparator );
										
					if(resultREST.length > 2 && 0 == Integer.parseInt(resultREST[2]))// No error
					{	actLog.removeFirst();
						counter = -1;
						
					}//removes the message from the local storage
					
					
					else if ( 0 < resultREST[1].length())//check network: server timeout, does not return type
					{	
						counter++;
						if(counter == 2)
						{
							actLog.removeFirst();
							counter = -1;
						}
					 }
					else//no network
					{
						actLog.removeFirst();
						break;
					}
				}
			//}
			
			try 
			{	
				Thread.sleep(freq);	
			} 
			catch (InterruptedException e) 
			{	
				logger.log("Server: InterruptedException");//actLog.addMessage(new ErrorMessage(e));
			}
		}
	}
	/*
	public String contactServer(String[] inputElements)
	{
		String separator 	= ",";
		StringBuffer result = new StringBuffer();
		
	    if (inputElements.length > 0) 
	    {
	        result.append(inputElements[0]);
	        for (int count=1; count<inputElements.length; count++) 
	        {
	            result.append(separator);
	            result.append(inputElements[count]);
	        }
	    }

		return contactServer(result.toString());
	}
	*/
	
	/**
	 * This method will sends a rest message to the server.
	 * It receives a reply message from the server.
	 * 
	 * @param inputMessage plan text massage in comma-separated values
	 * 
	 * @return a reply message from the server
	 */
	public Reply contactServer(Message inputMessage)
	{
		return contactServer(inputMessage.getREST());
	}
	
	public Reply contactServer(String inputMessage)
	{
		return contactServer(inputMessage,"","");
	}
	
	public Reply contactServer(String inputBody, String crc, String pic)
	{
		return new Reply(contactRESTServer(inputBody, crc, pic));
	}
	
	
	/**
	 * This method cleans, encrypts and sends the newly encrypted message to the server.
	 * It receives a reply message from the server, it decrypts the message and checks for errors.
	 * Depending on the outcome it returns the corresponding result
	 * 
	 * @param inputBody the rest message to be sent to the server
	 * @return reply from the server
	 */
	private String contactRESTServer(String inputBody, String crc, String pic)
	//public String contactServer(String inputBody)
	{
		//DEBUG: Andrews REST String
		//inputBody = "0,09,110701141039+01,0,15555215554,000000000000000,unknown,sdk,7,2,Android";
		logger.log("contactRESTServer1");
		boolean getFlag = true;
		//boolean messageOk = false;
		
		//logger.log("Before POST: SERVER:CRC="+crc+" ,SERVERHEX="+pic);
		
		if(null == crc || crc.equals("") 
		//|| 0 == pic.length )
		|| null == pic || pic.equals(""))
		{	getFlag = true;	}
		else
		{	getFlag = false;	}
			
		logger.log("SERVERBeforeEncrypt->:"+inputBody);
		inputBody =  tools.topAndTail(tools.stringToHex(encrypt(inputBody.trim())));//encrypt REST -> convert to HEX -> top&tail with HEX value
		logger.log("SERVERAfterEncrypt<-:"+inputBody);
		//logger.log("SERVERAfterEncrypt(Decrypted)<-:"+decrypt(inputBody));
		
		 String result = null; 

		 try
		 {
			//send HTTP request and save the response
	        //HttpConnection httpclient = null;
	    	//String URL = "http://217.115.115.148:8000/dev1/mobileminder.net/bbTESTws.php?HelloWorld";
	        //use API 5.0 Connection factory class to get first available connection
			String fullURL = URL + inputBody;
			//TODO: DEBUG
			//fullURL = "http://217.115.115.148:8000/dev1/mobileminder.net/WebService.php?71E51DB0994E00C097AD906EF77980A28FDD9DB0911E006C311831609FD780F01FD83DBC3568325EF77F6810977F625E156E00AC3FDE00AC3FDE00AC3FDE00AC356DC58886C33EF4C5626608856BFDB6B5620584AA8337E4A8";
			logger.log("contactRESTServer::fullURL is: "+fullURL);
			
			// Need this to allow internet on simulator
			if (DeviceInfo.isSimulator())
			{
				fullURL=fullURL+";deviceSide=true";
			}
			//Make device connect via wifi
			logger.log("Make sure it connects via wifi...");
			fullURL+=";inteface=wifi";
			
			logger.log("Setting up HTTP connection...");
			//HttpConnection httpclient = (HttpConnection) Connector.open(fullURL);
			HttpConnection httpclient = (HttpConnection) new ConnectionFactory().getConnection(fullURL).getConnection();
			logger.log("Connection set up!");
			
	        int len = (int) httpclient.getLength();
	        logger.log("length retrieved");
	        byte responseData[] = new byte[len];
	        DataInputStream dis = null;
			
			if(getFlag)
			{
				logger.log("Using GET");
				//httpclient.setRequestMethod(HttpConnection.GET);
				logger.log("GET set. Setting length...");
				//httpclient.setRequestProperty("Content-Length", ""+inputBody.length());
				logger.log("Length set!");
			}
			else
			{ 
				logger.log("Using POST");
				httpclient.setRequestMethod(HttpConnection.POST);
				// Not sure if this will work
				int length = inputBody.length()+crc.length()+pic.length();
				httpclient.setRequestProperty("Content-Length", ""+length);
				httpclient.setRequestProperty("crc", crc);
				httpclient.setRequestProperty("pic", pic);
				logger.log("In Send POST: SERVER:CRC="+crc+" SERVERHEX="+pic);
			}  // end if/else
			
			logger.log("Getting response message from server...");
	        logger.log("HTTP Code returned: "+httpclient.getResponseMessage());

			int status = httpclient.getResponseCode();
			if (status == HttpConnection.HTTP_OK)
			{
				logger.log("Now getting length of reply from server...");
		        //int len = (int) httpclient.getLength();
		        logger.log("contactRESTsever::Length of reply is: "+len);
		        //check if len is not -1, ie unknown. If it is, make the array size 10
		        //byte responseData[] = new byte[(len>=0?len:10)];
		        //DataInputStream dis = null;

		        logger.log("Opening data input stream...");
	        	dis = new DataInputStream(httpclient.openInputStream());
	        	logger.log("DataInputStream opened. Reading data...");
				dis.readFully(responseData);

		        result = new String(responseData);
		        
		        logger.log("Server-> "+result);
		     	
				if(null != result && tools.isHex(result))
				{
					result = decrypt(result);
					logger.log("DecryptedResultSERVER->:"+result);
				}
				else
		     	{
		     		result = serverErrorReply+"Corrupted Message";
		     	}
		     	//logger.log("**OUT if**");
			    live = true;
			    //logger.log("**LIVE = TRUE**");
			}
			else
			{
				logger.log("x:contactRESTServer::Reply from server::"+status);
			}		     
		 } // end try
		 catch (IOException e)
		 {
			 result = serverErrorReply+"Server Unreachable";
			 live = false;
			 logger.log("x::contactRESTserver::IOException::"+e.getMessage());
			 e.printStackTrace();
		 }
		/* catch (Exception e)//Request Timeout! 
		 {
			 logger.log("x::contactRESTServer::Exception::"+e.getMessage());
			 //logger.log(e.getMessage()+" "+e.getLocalizedMessage());
			 result = serverErrorReply+"Server Unreachable";
			 live = false;
			 
			 //No need to log
			 //actLog.addMessage(new ErrorMessage(e));
		 }*/		 
		 return result;
	}
	
	
	/**
	 * This method replaces characters in the inputString that can not be used in a URL with a "_" . 
	 * It uses the charmAt() method in the Security class to recognise the unusable characters.
	 * 
	 * Note: The underscores are then replaced with spaces on the server side
	 * 
	 * @param inputString The restString.
	 * @return an encapsulated Character array.
	 */
	/*
	private String cleanRESTString(String inputString)
	{
		char[] charArray = inputString.toCharArray();
		
		for(int count = 0; count < charArray.length; count++)
		{
			
			if(-1 == security.charAt(charArray[count]))
			{
				charArray[count] = (char)95;//_
			}
			
			switch(charArray[count])
			{
				case (char)32://space
				case (char)34://"
				case (char)35://#
				case (char)37://%
				case (char)39://'
				case (char)60://<
				case (char)62://>
				case (char)94://^
				case (char)96://`
				case (char)123://{
				case (char)124://|
				case (char)125://}
				charArray[count] = (char)95;//_
					break;
			}
		}
		return new String(charArray);
	}*/
	/**
	 * This method establishes the connection with the server.
	 */
	public void startup()
	{/*
		try {
			logger.log("In startup. Connecting to wrong URL!");
			httpclient = (HttpConnection) Connector.open(URL, Connector.READ_WRITE);
		} catch (IOException e) {
			logger.log("x::Error opening HTTP connection to server");
			e.printStackTrace();
		}*/
	}
	/**
	 * This method terminates the connection with the server.
	 */
	public void shutdown()
	{
		/*
		try {
			logger.log("In shutdown!");
			httpclient.close();
		} catch (IOException e) {
			logger.log("x::Error closing connection to HTTP server");
			e.printStackTrace();
		}*/
	}
	/**
	 * This method checks the status of the server.
	 * @return true if the server is live, otherwise is down.
	 */
	public boolean isLive()
	{	return live;	}
	/**
	 * This method encrypts the message.
	 * @param inputText message to be encrypted.
	 * @return an encrypted message.
	 */
	private String encrypt(String inputText)
	{
		logger.log("In encrypt(): "+inputText);
		inputText = getRandomString(generator.nextInt(10))//add random
					+Tools.RestElementSeparator
					+getCrcValue(inputText)//add CRC
					+Tools.RestElementSeparator
					+inputText;
		

		logger.log("String to be encrypted: "+inputText);
		return security.cryptFull(inputText, true);
	}
	
	public long getCrcValue(String inputText)
	{
		logger.log("In getCrcValue: "+inputText);
		int crc1 = 0;
		
		crc1 = CRC32.update(CRC32.INITIAL_VALUE, inputText.getBytes()) ^ 0xffffffff;

		
		//This loop will try stop incorrect CRCs, ie CRCs with a negative value
		for(int count=0 ; crc1<=0 ; count++)
		{
			// Reset CRC
			crc1=0;
			// Get new CRC
			crc1 = ~crc1;
			//crc1 = CRC32.update(CRC32.INITIAL_VALUE, inputText.getBytes());
			// Do the loop 10 times and if it still hasnt got a decent CRC, give up!
			if(count >= 10)
				crc1=1;
		}

		logger.log("CRC1 is: "+crc1);
		if (crc1==1)
			logger.log("x::Error with CRC. CRC is negative! Prepare for NullPointerException!!");
		
		//TODO: DEBUG. CRC FROM ANDROID
		//crc1=1903129755;
		
		//return (crc1<=0?abs(crc1):crc1);
		return crc1;
	}
	/**
	 * This method decrypts the message which was encrypted.
	 * @param inputText message to be decrypted.
	 * @return a decrypted message.
	 */
	private String decrypt(String inputText)
	{  
		String text = "";
		if(null == inputText || 0 == inputText.length())//messages with bad checksums will return blank
		{	
			logger.log("decrypt::No server Message to decrypt");
			return null;
		}
 		
 
		//Reverse top&tail -> convert to String -> decrypt REST
		String[]replyArray = Reply.stringToArray(security.cryptFull(hexToString(tools.reverseTopAndTail(inputText)),false));//.split(Server.RestElementSeparator);
		//for debugging
		for(int counts = 0; counts<replyArray.length; counts++)
		{
			logger.log("ReplyArray elements: "+replyArray[counts]);
		}
		//logger.log("DECRYPT: 389");
		//rebuild message
		logger.log("decrypt::replyArray size is: "+replyArray.length);
		for(int count = 2; count<replyArray.length; count++)
		{			
			text += replyArray[count];
			if((replyArray.length-1) > count)
			{	text += Tools.RestElementSeparator; }
		}
 
		int crc=0;//crc.reset();
//		crc = CRC32.update(CRC32.INITIAL_VALUE, text.getBytes());
		crc = CRC32.update(CRC32.INITIAL_VALUE, text.getBytes());

		// Ensure CRC is always positive/unsigned
		// Convert integer CRC to unsigned binary string
		String temp = Integer.toBinaryString(crc);
		long crcL = Long.parseLong(temp, 2);//tempInt.intValue();
		
		logger.log("Server CRC: "+Long.parseLong(replyArray[1]));
		logger.log("Client VAL: "+text);
		logger.log("Client CRC: "+crc);
		logger.log("testLong CRC:"+crcL);
		
		return text;
		/* THIS CODE IS NOT WORKING. NEED TO FIX CRC BEFORE IT WILL EVER BE "TRUE"
		if(Long.parseLong(replyArray[1]) == crc)//check CRC
		{				
		//	logger.log("DECRYPT: 405");
			return text;
		}
		else
		{	//logger.log("DECRYPT: 408");
			return null;	//TODO: This causes a NullPointerException in 
		} */
	}
	
	/**
	 * This method randomly shuffles the character set for encrypt Method.
	 * @param inputLength the length of the random character set to be created.
	 * @return a string with random characters.
	 */
	private String getRandomString(int inputLength)
	{
		logger.log("In getRandomString(int)");
		String returnString = "";
		for(int count = 0; count < inputLength; count++)
		{
			returnString += charSET.charAt(generator.nextInt(charSET.length())); 
		}
		
		logger.log("The Random number is: "+returnString);
		return returnString;
	}	
	
	/**
	 * This method formats a hex string into a String
	 * 
	 * @param hex hex string 
	 * @return String
	 */
	 public String hexToString(String hex)
	 {		 
		 StringBuffer output = new StringBuffer();
		 String str = "";
	     for (int i = 0; i < hex.length(); i+=2)
	     {
	         str = hex.substring(i, i+2);
	         output.append((char)Integer.parseInt(str, 16));
	     }
	     return output.toString();
	 }
}