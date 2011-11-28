package com.mmtechco.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.RadioInfo;

import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.enums.FILESYSTEM;

/**
 * Tools which are BlackBerry specific.
 */
public class ToolsBB extends Tools {
	private static ToolsBB instance = null;
	private static String OSVersion;
	private static Date startTime;

	private ToolsBB() {
	}

	public static MMTools getInstance() {
		if (null == instance) {
			instance = new ToolsBB();
		}
		return instance;
	}

	public long getUptimeInSec() {
		// Memoized for quicker lookup
		if (startTime == null) {
			/*
			 * Creates a dummy event which knows the device's uptime. Note that
			 * the Event uptime is returned as an int so there will be an
			 * overflow after 25 days. Hence putting it into a Date object.
			 */
			EventInjector.Event dummyEvent = new EventInjector.KeyEvent(0, 'c',
					0);
			dummyEvent.setTimeToCurrent(); // Get current uptime
			startTime = new Date(new Date().getTime() - dummyEvent.getTime());
		}
		return (new Date().getTime() - startTime.getTime()) / 1000;
	}

	public String getDate(long date) {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(date));
	}

	public long getDate(String date) throws Exception {
		date = date.trim();
		if (!(date.equals("0") || date.equals(""))) {
			// Yes this is horrible but so is RIM's SimpleDateFormat
			// which doesn't work properly.
			String year = date.substring(0, 4);
			String month = date.substring(4, 6);
			String day = date.substring(6, 8);
			String hour = date.substring(8, 10);
			String min = date.substring(10, 12);
			String sec = date.substring(12, 14);

			// Accepts in format "YYYY-MM-DDThh:mm:ss+00:00" (eg
			// 1997-07-16T19:20:30+01:00)
			String formattedDate = year + "-" + month + "-" + day + "T" + hour
					+ ":" + min + ":" + sec + "+00:00";

			return HttpDateParser.parse(formattedDate);
		}
		return 0;
	}

	public String getDeviceManufacturer() {
		return String.valueOf(Branding.getVendorId());
	}

	/**
	 * Returns major OS version
	 * 
	 * @return OS version, e.g. 5 instead of 5.03.23.
	 */
	public int getOSVersionGen() {
		// getOSVersion returns a string in form of x.yy.zz
		return Integer.parseInt((getOSVersion()).substring(0, 1));
	}

	/**
	 * Get the OS version of the device.
	 * 
	 * @return string of OS in form of x.yy.zz
	 */
	public String getOSVersion() {
		// OSVersion is memoized for quicker lookup
		if (OSVersion == null) {
			// Reference app to use: Ribbon App
			String refapp = "net_rim_bb_ribbon_app";
			ApplicationManager appMan = ApplicationManager
					.getApplicationManager();
			// Get running applications
			ApplicationDescriptor[] appDes = appMan.getVisibleApplications();
			for (int i = 0; i < appDes.length; i++) {
				if ((appDes[i].getModuleName()).equals(refapp)) {
					OSVersion = appDes[i].getVersion();
				}
			}
		}
		return OSVersion;
	}

	/**
	 * Checks if passed filesystem is present and mounted by enumerating root
	 * directory mounts.
	 * 
	 * @param FILESYSTEM
	 *            fs enumerated list of filesystems.
	 * @return boolean mounted
	 */
	public static boolean fsMounted(FILESYSTEM fs) {
		boolean mounted = false;
		String root;
		Enumeration e = FileSystemRegistry.listRoots();
		while (e.hasMoreElements()) {
			root = (String) e.nextElement();
			if (root.equalsIgnoreCase(fs.toString() + "/")) {
				mounted = true;
			}
		}
		return mounted;
	}

	public String[] split(String strString, String strDelimiter) {
		int iOccurrences = 0;
		int iIndexOfInnerString = 0;
		int iIndexOfDelimiter = 0;
		int iCounter = 0;

		if (strString == null) {
			throw new NullPointerException("Input string cannot be null.");
		}
		if (strDelimiter.length() <= 0 || strDelimiter == null) {
			throw new NullPointerException("Delimeter cannot be null or empty.");
		}

		// If strString begins with delimiter then remove it in order to comply
		// with the desired format.
		if (strString.startsWith(strDelimiter)) {
			strString = strString.substring(strDelimiter.length());
		}

		// If strString does not end with the delimiter then add it to the
		// string in order to comply with the desired format.
		if (!strString.endsWith(strDelimiter)) {
			strString += strDelimiter;
		}

		// Count occurrences of the delimiter in the string. Occurrences should
		// be the same amount of inner strings.
		while ((iIndexOfDelimiter = strString.indexOf(strDelimiter,
				iIndexOfInnerString)) != -1) {
			iOccurrences += 1;
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
		}
		String[] strArray = new String[iOccurrences];
		iIndexOfInnerString = 0;
		iIndexOfDelimiter = 0;

		// Walk across the string again and this time add the strings to the
		// array.
		while ((iIndexOfDelimiter = strString.indexOf(strDelimiter,
				iIndexOfInnerString)) != -1) {

			// Add string to array.
			strArray[iCounter] = strString.substring(iIndexOfInnerString,
					iIndexOfDelimiter);

			// Increment the index to the next character after the next
			// delimiter.
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();

			iCounter += 1;
		}
		return strArray;
	}

	public void addMsgToInbox(String message) {
		try {
			Address sender = new Address("info@mobileminder.com",
					"Mobile Minder");

			Session session = Session.waitForDefaultSession();
			Store store = session.getStore();
			Folder inbox = store.list(Folder.INBOX)[0];

			Message msg = new Message(inbox);
			msg.setContent(message);
			msg.setFrom(sender);
			msg.setStatus(Message.Status.RX_RECEIVED,
					Message.Status.RX_RECEIVED);
			msg.setSentDate(new Date());
			msg.setFlag(Message.Flag.REPLY_ALLOWED, true);
			msg.setInbound(true);
			msg.setSubject("Mobile Minder Registration Info");
			inbox.appendMessage(msg);
		} catch (MessagingException e) {
			logger.log(TAG, "Could not add message to inbox");
		}
	}

	public void sendSMS(String number, String message) throws IOException {
		// Note that send() is a blocking synchronous method so when calling
		// this method spawn a new thread.
		MessageConnection gsmConnection = null;
		DatagramConnection cdmaConnection = null;
		try {
			if (RadioInfo.getNetworkType() == RadioInfo.NETWORK_CDMA) {
				// CDMA network
				cdmaConnection = (DatagramConnection) Connector.open("sms://"
						+ number);
				byte[] data = message.getBytes();
				Datagram dg = cdmaConnection.newDatagram(cdmaConnection
						.getMaximumLength());
				dg.setData(data, 0, data.length);
				cdmaConnection.send(dg);
			} else {
				// GSM network
				gsmConnection = (MessageConnection) Connector.open("sms://"
						+ number);
				TextMessage bottle = (TextMessage) gsmConnection
						.newMessage(MessageConnection.TEXT_MESSAGE);
				bottle.setPayloadText(message);
				gsmConnection.send(bottle);
			}
		} finally {
			if (gsmConnection != null) {
				gsmConnection.close();
			}
			if (cdmaConnection != null) {
				cdmaConnection.close();
			}
		}
	}
	
	public HttpConnection setupConnection(String url) throws IOException {
		if (DeviceInfo.isSimulator()) {
			// If running the MDS simulator append ";deviceside=false"
			return (HttpConnection) Connector.open(url + ";deviceside=true",
					Connector.READ_WRITE);
		}
		ConnectionFactory cf = new ConnectionFactory();
		// Ordered list of preferred transports
		int[] transportPrefs = { TransportInfo.TRANSPORT_TCP_WIFI,
				TransportInfo.TRANSPORT_TCP_CELLULAR,
				TransportInfo.TRANSPORT_WAP2, TransportInfo.TRANSPORT_WAP,
				TransportInfo.TRANSPORT_MDS, TransportInfo.TRANSPORT_BIS_B };
		cf.setPreferredTransportTypes(transportPrefs);
		ConnectionDescriptor cd = cf.getConnection(url);
		return (HttpConnection) cd.getConnection();
	}
	
	/**
	 * Checks if there is a valid internet connection.
	 * 
	 * @return true if connected.
	 */
	public boolean isConnected() {
		String url = "http://www.msftncsi.com/ncsi.txt";
		String expectedResponse = "Microsoft NCSI";
		
		logger.log(TAG, "Checking connectivity");
		
		try {
			HttpConnection connection = setupConnection(url);
			connection.setRequestMethod(HttpConnection.GET);

			int status = connection.getResponseCode();
			if (status == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return expectedResponse.equals(reply);
			}
		} catch (Exception e) {
			logger.log(TAG, "Connectivity test failed");
		}
		return false;
	}
}