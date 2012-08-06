//#preprocess
package com.mmtechco.util;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
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
import net.rim.device.api.io.http.HttpDateParser;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;

import com.mmtechco.mobileminder.data.FILESYSTEM;

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

	public long getDate(String date) throws RuntimeException {
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
			Logger.getLogger(getClass()).info("Could not add message to inbox");
		}
	}

	public static void sendSMS(String number, String message) throws IOException {
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
	
	public static String getSimpleClassName(Class _class) {
		String classname = _class.getName();
		char packageSeparator = '.';
		int index = classname.lastIndexOf(packageSeparator);
		if (index != -1) {
			return classname.substring(index + 1, classname.length());
		}
		return classname;
	}
	
	/**
	 * Resizes a bitmap with an alpha channel (transparency) without the
	 * artifacts introduced by <code>Bitmap.scaleInto()</code>.
	 * 
	 * @param bitmap
	 *            Source Bitmap
	 * @param width
	 *            New Width
	 * @param height
	 *            New Height
	 * @param filterType
	 *            Filter quality to use. Can be
	 *            <code>Bitmap.FILTER_LANCZOS</code>,
	 *            <code>Bitmap.FILTER_BILINEAR</code> or
	 *            <code>Bitmap.FILTER_BOX</code>.
	 * @param aspectRatio
	 *            Specifies how the picture is resized. Can be
	 *            <code>Bitmap.SCALE_TO_FIT</code>,
	 *            <code>Bitmap.SCALE_TO_FILL</code> or
	 *            <code>Bitmap.SCALE_STRETCH</code>.
	 * @return The resized Bitmap in a new object.
	 */
	public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height,
			int filterType, int aspectRatio) {
		if (bitmap == null)
			return null;

		// Get the original dimensions of the bitmap
		int nOriginWidth = bitmap.getWidth();
		int nOriginHeight = bitmap.getHeight();
		if (width == nOriginWidth && height == nOriginHeight)
			return bitmap;

		// Prepare a drawing bitmap and graphic object
		Bitmap bmpOrigin = new Bitmap(nOriginWidth, nOriginHeight);
		Graphics graph = Graphics.create(bmpOrigin);

		// Create a line of transparent pixels for later use
		int[] aEmptyLine = new int[width];
		for (int x = 0; x < width; x++)
			aEmptyLine[x] = 0x00000000;
		// Create two scaled bitmaps
		Bitmap[] bmpScaled = new Bitmap[2];
		for (int i = 0; i < 2; i++) {
			// Draw the bitmap on a white background first, then on a black
			// background
			graph.setColor((i == 0) ? Color.WHITE : Color.BLACK);
			graph.fillRect(0, 0, nOriginWidth, nOriginHeight);
			graph.drawBitmap(0, 0, nOriginWidth, nOriginHeight, bitmap, 0, 0);

			// Create a new bitmap with the desired size
			bmpScaled[i] = new Bitmap(width, height);
			if (aspectRatio == Bitmap.SCALE_TO_FIT) {
				// Set the alpha channel of all pixels to 0 to ensure
				// transparency is
				// applied around the picture, if needed by the transformation
				for (int y = 0; y < height; y++)
					bmpScaled[i].setARGB(aEmptyLine, 0, width, 0, y, width, 1);
			}

			// Scale the bitmap
			bmpOrigin.scaleInto(bmpScaled[i], filterType, aspectRatio);
		}

		// Prepare objects for final iteration
		Bitmap bmpFinal = bmpScaled[0];
		int[][] aPixelLine = new int[2][width];

		// Iterate every line of the two scaled bitmaps
		for (int y = 0; y < height; y++) {
			bmpScaled[0].getARGB(aPixelLine[0], 0, width, 0, y, width, 1);
			bmpScaled[1].getARGB(aPixelLine[1], 0, width, 0, y, width, 1);

			// Check every pixel one by one
			for (int x = 0; x < width; x++) {
				// If the pixel was untouched (alpha channel still at 0), keep
				// it transparent
				if (((aPixelLine[0][x] >> 24) & 0xff) == 0)
					aPixelLine[0][x] = 0x00000000;
				else {
					// Compute the alpha value based on the difference of
					// intensity
					// in the red channel
					int nAlpha = ((aPixelLine[1][x] >> 16) & 0xff)
							- ((aPixelLine[0][x] >> 16) & 0xff) + 255;
					if (nAlpha == 0)
						aPixelLine[0][x] = 0x00000000; // Completely transparent
					else if (nAlpha >= 255)
						aPixelLine[0][x] |= 0xff000000; // Completely opaque
					else {
						// Compute the value of the each channel one by one
						int nRed = ((aPixelLine[0][x] >> 16) & 0xff);
						int nGreen = ((aPixelLine[0][x] >> 8) & 0xff);
						int nBlue = (aPixelLine[0][x] & 0xff);

						nRed = (int) (255 + (255.0 * ((double) (nRed - 255) / (double) nAlpha)));
						nGreen = (int) (255 + (255.0 * ((double) (nGreen - 255) / (double) nAlpha)));
						nBlue = (int) (255 + (255.0 * ((double) (nBlue - 255) / (double) nAlpha)));

						if (nRed < 0)
							nRed = 0;
						if (nGreen < 0)
							nGreen = 0;
						if (nBlue < 0)
							nBlue = 0;
						aPixelLine[0][x] = nBlue | (nGreen << 8) | (nRed << 16)
								| (nAlpha << 24);
					}
				}
			}

			// Change the pixels of this line to their final value
			bmpFinal.setARGB(aPixelLine[0], 0, width, 0, y, width, 1);
		}
		return bmpFinal;
	}
}