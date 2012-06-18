package com.mmtechco.mobileminder;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.ui.ObserverScreen;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Checks the registration stage that currently the device is in.
 */
public class Registration implements MobileMinderResource {
	private static Logger logger = Logger.getLogger(Registration.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	public static final long ID = StringUtilities
			.stringHashToLong(Registration.class.getName());

	public final static String KEY_STAGE = "registration_stage";
	public final static String KEY_ID = "registration_id";

	private final static int intervalShort = 1000 * 60 * 2; // 2 min
	private final static int intervalLong = 1000 * 60 * 60 * 24; // 24h
	
	private static final int REGISTERED = 2;

	private static int stage;
	private static String id;
	private static String status = r.getString(i18n_RegRequesting);

	private static Vector observers = new Vector();

	public static void checkStatus() {
		logger.info("Checking registration status");

		// Read details from storage to have something to display in case there
		// is no connectivity
		readDetails();

		try {
			// Contact server and get new values, if any, otherwise sleep
			logger.debug("Requesting reg details from server");

			Response response = Server.get(new RegistrationMessage(stage)
					.toString());
			Reply.Regular reply = new Reply.Regular(response.getContent());
			logger.debug("Registration reply: " + reply.content);
			if (reply.error) {
				scheduleRun(intervalShort);
				return;
			}

			// Read and process registration data
			stage = Integer.parseInt(reply.info);
			id = reply.id;
			storeDetails();

			// Schedule a registration check based on stage
			if (stage < REGISTERED) {
				logger.debug("Scheduling short run");
				scheduleRun(intervalShort);
			} else {
				startComponents();
				logger.debug("Scheduling long run");
				scheduleRun(intervalLong);
			}
		} catch (IOException e) {
			logger.warn("Connection problem: " + e.getMessage());
			scheduleRun(intervalShort);
		} catch (ParseException e) {
			ActivityLog.addMessage(new ErrorMessage(e));
			scheduleRun(intervalShort);
		}
	}

	private static void scheduleRun(int sleepTime) {
		updateStatus();
		new Timer().schedule(new TimerTask() {
			public void run() {
				checkStatus();
			}
		}, sleepTime);
	}

	private static void readDetails() {
		// Read registration data or set to default values
		PersistentObject regData = PersistentStore.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			if (regTable == null) {
				logger.debug("Populating with default values");
				// Populate with default values
				regTable = new Hashtable();
				regTable.put(KEY_STAGE, "0");
				stage = 0;
				regTable.put(KEY_ID, id = "0");
				// Store to device
				regData.setContents(regTable);
				regData.commit();
			} else {
				logger.debug("Reading details from storage");
				// Read values from storage
				stage = Integer.parseInt((String) regTable.get(KEY_STAGE));
				id = (String) regTable.get(KEY_ID);
			}
		}
	}

	private static void storeDetails() {
		PersistentObject regData = PersistentStore.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			regTable.put(KEY_STAGE, String.valueOf(stage));
			regTable.put(KEY_ID, id);
			// Store to device
			regData.setContents(regTable);
			regData.commit();
		}
		// Also store id in RuntimeStore so different processes can read it
		// since the BB class loader doesn't handle static class variables
		// properly
		RuntimeStore.getRuntimeStore().put(ID + 32, id);
		logger.debug("Stored details");
	}

	private static void startComponents() {
		// Check to see if haven't already started components
		// Using RuntimeStore because the BB class loader doesn't handle
		// static class variables properly
		Boolean started = (Boolean) RuntimeStore.getRuntimeStore().get(ID);
		if (started == null || !started.booleanValue()) {
			logger.debug("Components not started");
			if (ApplicationManager.getApplicationManager().postGlobalEvent(ID)) {
				logger.debug("Fired event to start components");
				RuntimeStore.getRuntimeStore().put(ID, new Boolean(true));
			}
		}
	}

	private static void updateStatus() {
		switch (stage) {
		case 0: // Initialization state
			status = r.getString(i18n_RegRequesting);
			break;
		case 1: // Has id but not activated
			status = r.getString(i18n_RegNotActivated);
			// TODO: put this into thread
			// tools.addMsgToInbox(r.getString(i18n_WelcomeMsg));
			break;
		case 2: // Trial
			status = r.getString(i18n_RegTrial);
			break;
		case 3: // Fully active
			status = r.getString(i18n_RegActive);
			break;
		}
		logger.info("Update status: " + stage + ";" + id + ";" + status);

		// Tell screens to update themselves
		notifyObservers();
	}

	private static void notifyObservers() {
		for (int i = 0; i < observers.size(); i++) {
			((ObserverScreen) observers.elementAt(i)).update();
		}
	}

	public static void addObserver(ObserverScreen screen) {
		observers.addElement(screen);
	}

	public static void removeObserver(ObserverScreen screen) {
		observers.removeElement(screen);
	}

	/**
	 * Get the device registration ID.
	 * 
	 * @return registration ID string. <strong>"0"</strong> if not available.
	 */
	public static String getRegID() {
		return (String) RuntimeStore.getRuntimeStore().get(ID + 32);
	}

	public static String getStatus() {
		return status;
	}
}

class RegistrationMessage extends Message {
	/**
	 * Message format:
	 * <ul>
	 * <li>Registration stage
	 * <li>Phone number of device
	 * <li>device id: unique hardware id
	 * <li>manufacturer
	 * <li>device model
	 * <li>OS version
	 * <li>app version
	 * <li>platform tag
	 * </ul>
	 */
	public RegistrationMessage(int stage) {
		super(Message.REGISTRATION, new String[] {
			ToolsBB.getInstance().getDate(),
			String.valueOf(stage),
			Phone.getDevicePhoneNumber(false),
			Integer.toHexString(DeviceInfo.getDeviceId()),
			String.valueOf(Branding.getVendorId()),
			DeviceInfo.getDeviceName(),
			DeviceInfo.getSoftwareVersion(),
			ApplicationDescriptor.currentApplicationDescriptor()
			.getVersion(), "BlackBerry" });
	}
}
