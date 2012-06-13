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
import com.mmtechco.util.ToolsBB;

public class Server {
	private static final String TAG = ToolsBB.getSimpleClassName(Server.class);

	private static final String URL = "https://www.mobileminder.net/WebService.php?";

	public static Response get(String queryString) throws IOException {
		Logger.log(TAG, "GET query string: " + queryString);
		
		// Setup connection and HTTP headers
		HttpConnection connection = setupConnection(URL + queryString);
		connection.setRequestMethod(HttpConnection.GET);

		// Construct reply
		return new Response(connection);
	}

	public static Response post(String queryString, Hashtable keyvalPairs) throws IOException {
		Logger.log(TAG, "POST data: " + queryString);
		
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
		Logger.log(TAG, "POST multipart query string: " + queryString);
		
		// Setup connection and HTTP headers
		HttpConnection connection = setupConnection(URL + queryString);
		connection.setRequestMethod(HttpConnection.POST);
		String boundary = Long.toString(System.currentTimeMillis());
		connection.setRequestProperty(
				HttpProtocolConstants.HEADER_CONTENT_TYPE,
				HttpProtocolConstants.CONTENT_TYPE_MULTIPART_FORM_DATA
						+ ";boundary=" + boundary);

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
		
		Logger.log(TAG, "Checking connectivity");
		
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
			Logger.log(TAG, "Connectivity test failed");
		}
		return false;
	}
}
