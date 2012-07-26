//#preprocess
package com.mmtechco.mobileminder.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.DeviceInfo;
//#ifdef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
import rimx.network.TransportDetective;
import rimx.network.URLFactory;
//#endif

import com.mmtechco.util.Logger;

public class HttpClient {
	private static final String URL = "https://www.mobileminder.net/WebService.php?";
	
	private static Logger logger = Logger.getLogger(HttpClient.class);

	public static Response get(String queryString) throws IOException {
		logger.debug("GET query string: " + queryString);

		// URL encode the query string
		String queryStringEncoded = URLUTF8Encoder.encode(queryString);

		// Setup connection and HTTP headers
		HttpConnection connection = setupConnection(URL + queryStringEncoded);
		connection.setRequestMethod(HttpConnection.GET);
		connection.setRequestProperty(
						HttpProtocolConstants.HEADER_CONTENT_TYPE,
						HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);

		// Construct reply
		return new Response(connection);
	}

	public static Response post(String queryString, Hashtable keyvalPairs) throws IOException {
		logger.debug("POST data: " + queryString);
		
		// Setup connection and HTTP headers
		HttpConnection connection = setupConnection(URL + queryString);
		connection.setRequestMethod(HttpConnection.POST);
		connection.setRequestProperty(
						HttpProtocolConstants.HEADER_CONTENT_TYPE,
						HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);

		// Add message body and set Content-Length
		URLEncodedPostData encPostData = new URLEncodedPostData("UTF-8", false);
		for (Enumeration e = keyvalPairs.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			encPostData.append(key, (String) keyvalPairs.get(key));
		}
		byte[] postData = encPostData.toString().getBytes("UTF-8");
		connection.setRequestProperty(
				HttpProtocolConstants.HEADER_CONTENT_LENGTH,
				String.valueOf(postData.length));

		// Send data via POST
		OutputStream output = connection.openOutputStream();
		output.write(postData);
		output.flush();

		// Construct reply
		return new Response(connection);
	}

	public static Response postMultiPart(String queryString,
			FileConnection file, String controlName) throws IOException {
		logger.debug("POST multipart query string: " + queryString);
		
		// Setup connection and HTTP headers
		HttpConnection connection = setupConnection(URL + queryString);
		connection.setRequestMethod(HttpConnection.POST);
		String boundary = Long.toString(System.currentTimeMillis());
		connection.setRequestProperty(
				HttpProtocolConstants.HEADER_CONTENT_TYPE,
				HttpProtocolConstants.CONTENT_TYPE_MULTIPART_FORM_DATA
						+ ";boundary=" + boundary);
		connection.setRequestProperty(
				HttpProtocolConstants.HEADER_CONTENT_LENGTH,
				String.valueOf(file.fileSize()));

		// Send data via POST
		OutputStream output = connection.openOutputStream();
		output.write(("\r\n--" + boundary + "\r\n").getBytes());
		output.write(("Content-Disposition: form-data; name=\"" + controlName + "\"; filename=\""
				+ file.getName() + "\"\r\n").getBytes());
		output.write(("Content-Type: "
				+ MIMETypeAssociations.getNormalizedType(file.getPath()
						+ file.getName()) + "\r\n\r\n").getBytes());
		output.write(IOUtilities.streamToBytes(file.openInputStream()));
		output.write(("\r\n--" + boundary + "--\r\n").getBytes());

		/*
		// Other, less manual, method
		// Note: This doesn't work as setData overwrites all post data
		MultipartPostData postData = new MultipartPostData(
				MultipartPostData.DEFAULT_CHARSET, true);
		postData.setData(IOUtilities.streamToBytes(file.openInputStream());
		postData.append("name", controlName);
		postData.append("filename", file.getName());
		output.write(postData.getBytes());
		*/
		output.flush();

		// Construct reply
		return new Response(connection);
	}

	private static  HttpConnection setupConnection(String url) throws IOException {
		if (DeviceInfo.isSimulator()) {
			// If running the MDS simulator append ";deviceside=false"
			return (HttpConnection) Connector.open(url + ";deviceside=true",
					Connector.READ_WRITE);
		}
		//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0
		ConnectionFactory cf = new ConnectionFactory();
		// Ordered list of preferred transports
		int[] transportPrefs = { TransportInfo.TRANSPORT_TCP_WIFI,
				TransportInfo.TRANSPORT_TCP_CELLULAR,
				TransportInfo.TRANSPORT_WAP2, TransportInfo.TRANSPORT_WAP,
				TransportInfo.TRANSPORT_MDS, TransportInfo.TRANSPORT_BIS_B };
		cf.setPreferredTransportTypes(transportPrefs);
		ConnectionDescriptor cd = cf.getConnection(url);
		return (HttpConnection) cd.getConnection();
		//#else
		TransportDetective td = new TransportDetective();
		URLFactory urlFactory = new URLFactory(url);
		String connectionUrl;
		if(td.isCoverageAvailable(TransportDetective.TRANSPORT_TCP_WIFI)) {
		   connectionUrl = urlFactory.getHttpTcpWiFiUrl();
		} else if (td.isCoverageAvailable(TransportDetective.DEFAULT_TCP_CELLULAR)) {
			connectionUrl = urlFactory.getHttpDefaultTcpCellularUrl(td.getDefaultTcpCellularServiceRecord());
		} else if (td.isCoverageAvailable(TransportDetective.TRANSPORT_WAP2)) {
			connectionUrl = urlFactory.getHttpWap2Url(td.getWap2ServiceRecord());
		} else if (td.isCoverageAvailable(TransportDetective.TRANSPORT_MDS)) {
			connectionUrl = urlFactory.getHttpMdsUrl(false);
		} else if (td.isCoverageAvailable(TransportDetective.TRANSPORT_BIS_B)) {
			connectionUrl = urlFactory.getHttpBisUrl();
		} else {
			connectionUrl = urlFactory.getHttpDefaultUrl();
		}
		return (HttpConnection)Connector.open(connectionUrl, Connector.READ_WRITE);
		//#endif
	}
	
	/**
	 * Checks if there is a valid internet connection.
	 * 
	 * @return true if connected.
	 */
	public static boolean isConnected() {
		String url = "http://www.msftncsi.com/ncsi.txt";
		String expectedResponse = "Microsoft NCSI";
		
		logger.debug("Checking connectivity");
		
		try {
			HttpConnection connection = setupConnection(url);
			connection.setRequestMethod(HttpConnection.GET);

			int status = connection.getResponseCode();
			if (status == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return expectedResponse.equals(new String(reply));
			}
		} catch (Exception e) {
			logger.warn("Connectivity test failed");
		}
		return false;
	}
}

/**
 * Provides a method to encode any string into a URL-safe
 * form.
 * Non-ASCII characters are first encoded as sequences of
 * two or three bytes, using the UTF-8 algorithm, before being
 * encoded as %HH escapes.
 *
 * Created: 17 April 1997
 * Author: Bert Bos <bert@w3.org>
 *
 * URLUTF8Encoder: http://www.w3.org/International/URLUTF8Encoder.java
 *
 * Copyright © 1997 World Wide Web Consortium, (Massachusetts
 * Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. 
 * This work is distributed under the W3C® Software License [1] in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

class URLUTF8Encoder
{

  final static String[] hex = {
    "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
    "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
    "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
    "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
    "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
    "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
    "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
    "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
    "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
    "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
    "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
    "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
    "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
    "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
    "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
    "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
    "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
    "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
    "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
    "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
    "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
    "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
    "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
    "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
    "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
    "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
    "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
    "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
    "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
    "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
    "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
    "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
  };

  /**
   * Encode a string to the "x-www-form-urlencoded" form, enhanced
   * with the UTF-8-in-URL proposal. This is what happens:
   *
   * <ul>
   * <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z',
   *        and '0' through '9' remain the same.
   *
   * <li><p>The unreserved characters - _ . ! ~ * ' ( ) remain the same.
   *
   * <li><p>The space character ' ' is converted into a plus sign '+'.
   *
   * <li><p>All other ASCII characters are converted into the
   *        3-character string "%xy", where xy is
   *        the two-digit hexadecimal representation of the character
   *        code
   *
   * <li><p>All non-ASCII characters are encoded in two steps: first
   *        to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
   *        secondly each of these bytes is encoded as "%xx".
   * </ul>
   *
   * @param s The string to be encoded
   * @return The encoded string
   */
  public static String encode(String s)
  {
    StringBuffer sbuf = new StringBuffer();
    int len = s.length();
    for (int i = 0; i < len; i++) {
      int ch = s.charAt(i);
      if ('A' <= ch && ch <= 'Z') {		// 'A'..'Z'
        sbuf.append((char)ch);
      } else if ('a' <= ch && ch <= 'z') {	// 'a'..'z'
	       sbuf.append((char)ch);
      } else if ('0' <= ch && ch <= '9') {	// '0'..'9'
	       sbuf.append((char)ch);
      } else if (ch == ' ') {			// space
	       sbuf.append('+');
      } else if (ch == '-' || ch == '_'		// unreserved
          || ch == '.' || ch == '!'
          || ch == '~' || ch == '*'
          || ch == '\'' || ch == '('
          || ch == ')') {
        sbuf.append((char)ch);
      } else if (ch <= 0x007f) {		// other ASCII
	       sbuf.append(hex[ch]);
      } else if (ch <= 0x07FF) {		// non-ASCII <= 0x7FF
	       sbuf.append(hex[0xc0 | (ch >> 6)]);
	       sbuf.append(hex[0x80 | (ch & 0x3F)]);
      } else {					// 0x7FF < ch <= 0xFFFF
	       sbuf.append(hex[0xe0 | (ch >> 12)]);
	       sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
	       sbuf.append(hex[0x80 | (ch & 0x3F)]);
      }
    }
    return sbuf.toString();
  }

}
