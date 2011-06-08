package com.spotlight.track;

/*
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.kids.Controller;
import com.kids.Logger;
import com.kids.R;
*/
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.List;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.io.messaging.Headers;
import net.rim.device.api.util.CRC32;

import com.kids.net.Reply;
import com.kids.net.Security;
import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataReader;
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
public class Server extends Thread
{
	public static final String RestElementSeparator = ",";
	private Debug logger = Logger.getInstance();
	/*
	private final String ServerFeedBack_good = "ok";
	private final String ServerFeedBack_Stop = "no";
	private final String ServerFeedBack_Bad  = "bad";
	*/
	
	//private final int		HeartBeat = 99;
	private LocalDataReader actLog;
	private final String 	URL;
	private HttpConnection	httpclient;
//	private String 	 		deviceId;
	private int 		 	freq = 1000 * 15; //freq = 1000 * 60 * 5; //5 min
	private boolean			live;
	private String serverErrorReply;
	private Random generator;
	private Security security;
	private final String charSET = "!$&()*+-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]_abcdefghijklmnopqrstuvwxyz~";  
	private CRC32 crc;
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
		//URL 	 = "http://192.168.81.1/kids/webservice.php?";
//		deviceId = "xxxxx";
		serverErrorReply =  RestElementSeparator+
		 					RestElementSeparator+//reply
		 				 1 +RestElementSeparator+//error
							RestElementSeparator;//CallingCODE
		//	Controller.getString(R.string.Error_ServerTimeOut);
		
		

		actLog 	 = inputActLog;
		//httpclient = new DefaultHttpClient();
		live = true;
		generator = new Random();
		crc = new CRC32();
		startup();
		
		logger.log("Started Server");

	}
	
	/**
	 * This method monitors the local storage for new messages stored at specific intervals and sends them to the server.
	 *  
	 */
	public void run()
	{
		
		logger.log("Server running"+actLog.length());
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
					resultREST = contactServer(actLog.getFirst()).getREST().split(RestElementSeparator);

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
		logger.log("contactRESTServer1");
		boolean getFlag = true;
		
		logger.log("Before POST: SERVER:CRC="+crc+" SERVERHEX="+pic);
		
		if(null == crc || crc.equals("") 
		//|| 0 == pic.length )
		|| null == pic || pic.equals(""))
		{	getFlag = true;	}
		else
		{	getFlag = false;	}
		
		/*
		 //Use to test decryption
		  
		//String output = decrypt("10A451DB09778325F294F60FEFFD98146B89F62528FDE0DBC356E0DBC356E08");
		//logger.log("\n\nThis is the result for 1: "+output);
		//String output2 = decrypt("10DD939F09689814EF11F6FF6377D925C3568381A377F6DBC3FD980A28FD980A28FDe");
		//logger.log("\n\nThis is the result for 2: "+output2);
		String output3 = decrypt("8928470E3129542F24C98142C7AFC0C2CA91725EF568381A377F6DBC311981409FDF681EF11F62509895175C3119816A3927881E17A985CAFB67DEF287A988221017DDBA310");
		logger.log("\n\nThis is the result for 3: "+output3);
		String output4 = decrypt("104034949DB49814EF11F6FF6377D925C3568381A377F6DBC3FD980A28FD980A28FDa");
		logger.log("\n\nThis is the result for 4: "+output4);
		logger.log("\n\nend");
		*/
		
		logger.log("SERVERBeforeEncrypt->:"+inputBody);
		inputBody =  topAndTail(stringToHex(encrypt(inputBody.trim())));//encrypt REST -> convert to HEX -> top&tail with HEX value
		logger.log("SERVERAfterEncrypt<-:"+inputBody);
		logger.log("SERVERAfterEncrypt(Decrypted)<-:"+decrypt(inputBody));
		
		 String result = null; 
		 //HttpGet  request  = new HttpGet (URL + inputBody.toUpperCase());
		 //HttpPost request2 = new HttpPost(URL + inputBody.toUpperCase());
		 
		 //Open connection
		 httpclient = (HttpConnection) Connector.open(URL, Connector.READ_WRITE);
		
		 //ResponseHandler<String> handler = new BasicResponseHandler();  
		 try
		 {
			 if(getFlag)
			 {	// GET
				 httpclient.setRequestMethod(HttpConnection.GET);
				 httpclient.setRequestProperty("Content-Length", ""+inputBody.length());
				 OutputStream os = httpclient.openOutputStream();
				 os.write(inputBody.getBytes("UTF-8"));
				 os.flush();
				 os.close();
				 //result = httpclient.execute(request, handler);
			 }
			 else
			 {  // POST
				 httpclient.setRequestMethod(HttpConnection.POST);
				 
				 ////////// Is this necessary??
				 // Headers: Possible way to store key value pairs in Blackberry
				 Headers nameValuePairs;
				 nameValuePairs.setHeader("crc", crc);
				 logger.log("CRC sent to server:"+crc);
				 nameValuePairs.setHeader("pic", pic);
				 ///////////////////////////////
				 
				 
				 OutputStream os = httpclient.openOutputStream();
				 os.write(inputBody.getBytes());
				 os.flush();
				 
				 // Object for encoding URL, replacing special chars and spaces. 
				 // format: id=zx&name=john&foo=bar
				 //URLEncodedPostData postData;
				 
				 /*
				 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			     nameValuePairs.add(new BasicNameValuePair("crc", crc ));
			     logger.log("CRC Sent to Server:"+crc);
			     nameValuePairs.add(new BasicNameValuePair("pic", pic));
			     
			     try 
			     {	request2.setEntity(new UrlEncodedFormEntity(nameValuePairs));	} 
			     catch (UnsupportedEncodingException e1) 
			     {logger.log("Server: UnsupportedEncodingException");}//actLog.addMessage(new ErrorMessage(e1));}
			     */
			     logger.log("In Send POST: SERVER:CRC="+crc+" SERVERHEX="+pic);
			     
			     //result = httpclient.execute(request2, handler);
			 }
			 

			 result=httpclient.getResponseMessage();
		     	

	     	 logger.log("ResultSERVER->:"+result);
	     	 //logger.log("**Before decrypt**");		     	 
	     	 result = decrypt(result);
	     	 //logger.log("**After decrypt**");
	     	 
	     	 logger.log("DecryptedResultSERVER->:"+result);
	     	
	     	 if(null == result || result.equals(""))
	     	 {
	     		 //result = serverErrorReply+Controller.getString(R.string.Error_CorruptedMessage);
	     		result = serverErrorReply+"x";
	     	 }
	     	 //logger.log("**OUT if**");
		     live = true;
		     //logger.log("**LIVE = TRUE**");
		     
		     
		 }	 // End try() 
		 /*
		 catch (HttpResponseException e)
		 { Controller.error(e.getMessage()); }
		 catch (ClientProtocolException e) 
		 { Controller.error(e.getMessage()); } */
		 catch (Exception e)//Request Timeout! 
		 { 
			 //logger.log(e.getMessage()+" "+e.getLocalizedMessage());
			 result = serverErrorReply+Controller.getString(R.string.Error_ServerTimeOut);
			 live = false;
			 
			 //No need to log
			 //actLog.addMessage(new ErrorMessage(e));
		 }
		 
		 return result;
	}
	
	
	/**
	 * This method replaces characters in the inputString that can not be used in a URL with a "_" . 
	 * It uses the chatAt() method in the Security class to recognise the unusable characters.
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
	{	httpclient = new DefaultHttpClient();	}
	/**
	 * This method terminates the connection with the server.
	 */
	public void shutdown()
	{	httpclient.getConnectionManager().shutdown();	}
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
		
		inputText = getRandomString(generator.nextInt(10))//add random
					+RestElementSeparator
					+getCrcValue(inputText)//add CRC
					+RestElementSeparator
					+inputText;
		

		return security.cryptFull(inputText, true);
	}
	
	public long getCrcValue(String inputText)
	{
		//logger.log("In get CRC Values");
		crc.reset();
		crc.update(inputText.getBytes());
		return crc.getValue();
	}
	/**
	 * This method decrypts the message which was encrypted.
	 * @param inputText message to be decrypted.
	 * @return an decrypted message.
	 */
	private String decrypt(String inputText)
	{  
		String text = "";
		if(null == inputText || 0 == inputText.length())//messages with bad checksums will return blank
		{	return null;	}
 
	//	logger.log("DECRYPT: 382");
		
		crc.reset();
 
		//logger.log("DECRYPT: 386");		//Reverse top&tail -> convert to String -> decrypt REST
		String[]replyArray = Reply.stringToArray(security.cryptFull(hexToString(reverseTopAndTail(inputText)),false));//.split(Server.RestElementSeparator);
		//for debugging
		/*for(int counts = 0; counts<replyArray.length; counts++)
		{
			logger.log("ReplyArray elements: "+replyArray[counts]);
		}*/
		//logger.log("DECRYPT: 389");
		//rebuild message
		for(int count = 2; count<replyArray.length; count++)
		{
			
			//logger.log("DECRYPT: 394");
			text += replyArray[count];
			if((replyArray.length-1) > count)
			{	text += Server.RestElementSeparator; }
		}
 
		//logger.log("DECRYPT: 400");
		crc.update(text.getBytes());
		logger.log("Server CRC: "+Long.parseLong(replyArray[1]));
		logger.log("Clinet VAL: "+text);
		logger.log("Clinet CRC: "+crc.getValue());
		if(Long.parseLong(replyArray[1]) == crc.getValue())//check CRC
		{	
			
		//	logger.log("DECRYPT: 405");
			return text;	}
		else
		{	//logger.log("DECRYPT: 408");
			return null;	
		} 
	}
	
	/**
	 * This method randomly shuffles the character set for encrypt Method.
	 * @param inputLength the length of the random character set to be created.
	 * @return a string with random characters.
	 */
	private String getRandomString(int inputLength)
	{
		String returnString = "";
			for(int count = 0; count < inputLength; count++)
			{	returnString += charSET.charAt(generator.nextInt(charSET.length())); }
		return returnString;
	}
	
	/**
	 * This method formats a string into a hex string
	 * 
	 * @param b string
	 * @return hex string
	 */
		 public static String stringToHex(String s)
		 {
			char[] b;
			b = s.toCharArray();
		//s	s.getBytes();
			 
		    StringBuffer sb = new StringBuffer(b.length * 2);
		    for (int i = 0; i < b.length; i++) 
		    {	int v = b[i] & 0xff;
		    	if (v < 16){	sb.append('0');}
		    	sb.append(Integer.toHexString(v));
		    }
		    	return sb.toString().toUpperCase();
		 }
	
	
	/**
	 * This method formats a hex string into a String
	 * 
	 * @param hex hex string 
	 * @return String
	 */
	 public String hexToString(String hex){
		  
		 StringBuilder output = new StringBuilder();
		 String str = "";
		    for (int i = 0; i < hex.length(); i+=2) {
		        str = hex.substring(i, i+2);
		        output.append((char)Integer.parseInt(str, 16));
		    }
		    return output.toString();
	  }
	 
	
	/**
	 * This method formats a HEX string, adding a random HEX value to the start and end of the string
	 * 
	 * @param _input HEX string
	 * @return hex string
	 */
	public static String topAndTail(String _input)
	{
		Random rand = new Random();
		int top = rand.nextInt(16);
		int tail = rand.nextInt(16);
		String hexTop = Integer.toHexString(top);
		String hexTail = Integer.toHexString(tail);
		//Top=======Hex_string=======tail
		hexTop = hexTop.concat(_input).concat(hexTail);
		//logger.log("topAndTail:returns:"+hexTop);
		return hexTop;
	}
	
	/**
	 * This method formats a HEX string, removing a random HEX value from the start and end of the string
	 * 
	 * @param _input HEX string
	 * @return hex string
	 */
	public static String reverseTopAndTail(String _input)
	{
		String returnString = _input.substring(1, (_input.length() - 1));
		//logger.log("reversetopAndTail:returns:"+returnString);
		return returnString;
	}

	
}
