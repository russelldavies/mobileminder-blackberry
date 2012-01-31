package com.mmtechco.mobileminder.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.browser.MultipartPostData;
import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.http.HttpProtocolConstants;
import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.prototypes.MMServer;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.util.CRC32;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Monitors for new actions stored in the local storage for recording actions
 * and sends them to the web server at specific intervals.
 */
public class Server extends Thread implements MMServer, MobileMinderResource {
	private static final String TAG = ToolsBB.getSimpleClassName(Server.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private Logger logger = Logger.getInstance();
	private MMTools tools = ToolsBB.getInstance();
	private final String URL = "http://www.mobileminder.net/WebService.php?";
	private int freq = 1000 * 30; // 30 seconds
	private String serverErrorReply = Tools.ServerQueryStringSeparator
			+ Tools.ServerQueryStringSeparator + 1
			+ Tools.ServerQueryStringSeparator
			+ Tools.ServerQueryStringSeparator;
	private Security security;
	private CRC32 crc;

	/**
	 * Initializes server parameters, creates a new security instance and starts
	 * the server connection.
	 */
	public Server() {
		security = new Security();
		crc = new CRC32();
		logger.log(TAG, "Started");
	}

	/**
	 * Monitors the local storage for new messages stored at specific intervals
	 * and sends them to the server.
	 */
	public void run() {
		while (true) {
			String[] serverReply = null;
			int counter = -1;
			if (tools.isConnected()) {
				logger.log(TAG,
						"Checking for new messages to send. Message queue length: "
								+ ActivityLog.length());
				// Check is a message is in the local storage
				while (ActivityLog.length() > 0 && tools.isConnected()) {
					try {
						// Send the first message from the queue to the server
						// and parse reply
						serverReply = tools.split(
								get(ActivityLog.getMessage()),
								Tools.ServerQueryStringSeparator);
						// No error
						if (serverReply.length > 2
								&& (Integer.parseInt(serverReply[2]) == 0)) {
							// Pop the message off the queue
							ActivityLog.removeMessage();
							counter = -1;
						}
					} catch (NullPointerException e) {
						logger.log(TAG, "Could not contact server.");
					}

					if (null == serverReply) {
						logger.log(TAG,
								"Did not receive server reply, sleeping");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						// Check network: server timeout, does not return type
						if (0 < serverReply[1].length()) {
							counter++;
							if (counter == 2) {
								ActivityLog.removeMessage();
								counter = -1;
							}
						} else {
							// No network
							ActivityLog.removeMessage();
							break;
						}
					}
				}
			}
			// Sleep so loop doesn't spin
			try {
				Thread.sleep(freq);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Reply contactServer(Message inputMessage) {
		return contactServer(inputMessage.getREST());
	}

	public Reply contactServer(String inputMessage) {
		return new Reply(get(inputMessage));
	}

	public Reply contactServer(String inputBody, String crc, String pic) {
		return new Reply(post(inputBody, crc, pic));
	}

	public Reply contactServer(String inputBody, FileConnection pic) {
		return new Reply(postMultiPart(inputBody, pic));
	}

	/**
	 * Performs a HTTP GET request
	 * 
	 * @param queryString
	 *            - the query string that is appended to the URL that contains
	 *            data to be passed to the server.
	 * @return the server reply.
	 */
	public String get(String queryString) {
		logger.log(TAG, "GET query string: " + queryString);
		try {
			HttpConnection connection = setupConnection(URL
					+ encodeQueryString(queryString));
			connection.setRequestMethod(HttpConnection.GET);

			int status = connection.getResponseCode();
			if (status == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return processReply(new String(reply));
			} else {
				return serverErrorReply + r.getString(i18n_ErrorCorruptedMsg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(TAG, e.toString());
			return serverErrorReply + r.getString(i18n_ErrorServer);
		}
	}

	/**
	 * Performs a HTTP POST request passing the data in the message body URL
	 * encoded.
	 * 
	 * @param queryString
	 *            - the query string that is appended to the URL that contains
	 *            data to be passed to the server.he
	 * @param crc
	 *            - CRC of the file.
	 * @param pic
	 *            - picture file converted to a hex stream.
	 * @return the server reply.
	 */
	private String post(String queryString, String crc, String pic) {
		if (!tools.isConnected()) {
			return serverErrorReply + r.getString(i18n_ErrorCorruptedMsg);
		}
		logger.log(TAG, "POST query string: " + queryString);
		try {
			HttpConnection connection = setupConnection(URL
					+ encodeQueryString(queryString));
			connection.setRequestMethod(HttpConnection.POST);
			connection
					.setRequestProperty(
							HttpProtocolConstants.HEADER_CONTENT_TYPE,
							HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);

			URLEncodedPostData encPostData = new URLEncodedPostData("UTF-8",
					false);
			encPostData.append("crc", crc);
			encPostData.append("pic", pic);
			byte[] postData = encPostData.toString().getBytes("UTF-8");
			connection.setRequestProperty(
					HttpProtocolConstants.HEADER_CONTENT_LENGTH,
					String.valueOf(postData.length));

			// Send data via POST
			OutputStream output = connection.openOutputStream();
			output.write(postData);
			output.flush();

			// Read response
			if (connection.getResponseCode() == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return processReply(new String(reply));
			} else {
				return serverErrorReply + r.getString(i18n_ErrorCorruptedMsg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(TAG, e.toString());
			return serverErrorReply + r.getString(i18n_ErrorServer);
		}
	}

	/**
	 * Performs a HTTP POST request passing the data in the message body using
	 * multipart MIME.
	 * 
	 * @param queryString
	 *            - the query string that is appended to the URL that contains
	 *            data to be passed to the server.he
	 * @param pic
	 *            - the picture file.
	 * @return the server reply.
	 */
	private String postMultiPart(String queryString, FileConnection pic) {
		// Don't start if no connection
		if (!tools.isConnected()) {
			return serverErrorReply + r.getString(i18n_ErrorCorruptedMsg);
		}
		logger.log(TAG, "POST multipart query string: " + queryString);
		try {
			// Open file and stream to byte stream
			InputStream is = pic.openInputStream();
			byte[] fileData = IOUtilities.streamToBytes(is);
			// Setup connection
			HttpConnection connection = setupConnection(URL
					+ encodeQueryString(queryString));
			connection.setRequestMethod(HttpConnection.POST);
			String boundary = Long.toString(new Date().getTime()); // Uniqueness
			connection.setRequestProperty(
					HttpProtocolConstants.HEADER_CONTENT_TYPE,
					HttpProtocolConstants.CONTENT_TYPE_MULTIPART_FORM_DATA
							+ ";boundary=" + boundary);
			connection.setRequestProperty(
					HttpProtocolConstants.HEADER_CONTENT_LENGTH,
					String.valueOf(fileData.length));
			connection.setRequestProperty("xim-rim-transcode-content", "none");
			// Create payload
			MultipartPostData postData = new MultipartPostData("UTF-8", true);
			postData.setData(fileData);
			// Send data via POST
			OutputStream output = connection.openOutputStream();
			output.write(postData.getBytes());
			// Read response
			if (connection.getResponseCode() == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return processReply(new String(reply));
			} else {
				return serverErrorReply + r.getString(i18n_ErrorCorruptedMsg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(TAG, e.toString());
			return serverErrorReply + r.getString(i18n_ErrorServer);
		}
	}

	private String encodeQueryString(String queryString) {
		// Encrypt and convert to hex
		return tools.topAndTail(
				tools.stringToHex(encrypt(tools.safeRangeTextUTF(queryString
						.trim())))).toUpperCase();
	}

	private String processReply(String reply) {
		if (reply != null && tools.isHex(reply)) {
			return decrypt(reply);
		} else {
			return serverErrorReply + r.getString(i18n_ErrorCorruptedMsg);
		}
	}

	public long getCrcValue(String inputText) {
		return getCrcValue(inputText.getBytes());
	}

	public long getCrcValue(byte[] inputText) {
		crc.reset();
		crc.update(inputText);
		return crc.getValue();
	}

	/**
	 * Encrypts the message.
	 * 
	 * @param inputText
	 *            - message to be encrypted.
	 * @return an encrypted message.
	 */
	private String encrypt(String inputText) {
		// Add random text and CRC
		inputText = tools.getRandomString(new Random().nextInt(10))
				+ Tools.ServerQueryStringSeparator + getCrcValue(inputText)
				+ Tools.ServerQueryStringSeparator + inputText;
		return security.cryptFull(inputText, true);
	}

	/**
	 * Decrypts the message which was encrypted.
	 * 
	 * @param inputText
	 *            - message to be decrypted.
	 * @return an decrypted message.
	 */
	private String decrypt(String inputText) {
		String text = "";
		// Messages with bad checksums will return blank
		if (null == inputText || 0 == inputText.length()) {
			return null;
		}

		crc.reset();
		String[] replyArray = Reply.stringToArray(security.cryptFull(
				tools.hexToString(tools.reverseTopAndTail(inputText)), false));

		// rebuild message
		for (int count = 2; count < replyArray.length; count++) {
			text += replyArray[count];
			if ((replyArray.length - 1) > count) {
				text += Tools.ServerQueryStringSeparator;
			}
		}
		crc.update(text.getBytes());
		//logger.log(TAG, "Server CRC: " + Long.parseLong(replyArray[1]));
		//logger.log(TAG, "Client CRC: " + crc.getValue());
		logger.log(TAG, "Decrypted server reply: " + text);
		// Check CRC
		if (Long.parseLong(replyArray[1]) == crc.getValue()) {
			return text;
		} else {
			return null;
		}
	}

	private HttpConnection setupConnection(String url) throws IOException {
		return ((ToolsBB) ToolsBB.getInstance()).setupConnection(url);
	}
}
