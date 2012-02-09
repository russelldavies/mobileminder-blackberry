package com.mmtechco.mobileminder;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.MMServer;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.prototypes.ObserverScreen;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.util.ErrorMessage;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Checks the registration stage that currently the device is in.
 */
public class Registration extends Thread implements Controllable,
		MobileMinderResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(Registration.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	public static final long ID = StringUtilities
			.stringHashToLong(Registration.class.getName());

	public static String KEY_STAGE = "registration_stage";
	public static String KEY_ID = "registration_id";
	public static String KEY_NUMBERS = "emergency_numbers";

	private static int regStage;
	private static String regID;
	private static String emergNums;
	private static String status;
	private final int sleepTimeLong = 1000 * 60 * 60 * 24; // 24h
	private final int sleepTimeShort = 1000 * 60 * 2; // 2 min

	private MMServer server;
	private Logger logger = Logger.getInstance();
	private ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	private static Vector observers = new Vector();

	/**
	 * Initializes context, creates own instance of Server. Requests a
	 * registration ID and the and the registration state of the current device
	 * from server.
	 */
	public Registration() {
		logger.log(TAG, "Started");
		server = new Server();

		// Read registration data or set to default values
		PersistentObject regData = PersistentStore
				.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			if (regTable == null) {
				// Populate with default values
				regTable = new Hashtable();
				regTable.put(KEY_STAGE, "0");
				regTable.put(KEY_ID, "0");
				regTable.put(KEY_NUMBERS, "");
				// Store
				regData.setContents(regTable);
				regData.commit();
			}
			regStage = Integer
					.parseInt(String.valueOf(regTable.get(KEY_STAGE)));
			regID = String.valueOf(regTable.get(KEY_ID));
			emergNums = String.valueOf(regTable.get(KEY_NUMBERS));
		}
	}

	/**
	 * 
	 * Constantly checks the account status of the device at defined intervals.
	 * Checks if the SIM card of the device has been unlocked.
	 */
	public void run() {
		logger.log(TAG, "Running");

		boolean newState = true;
		Reply response;
		int nextStage;
		int currentStageValue = regStage;
		int time = 0;

		stageState(regStage);
		logger.log(TAG, "Registration stage: " + regStage);

		while (regStage < 2) {
			currentStageValue = regStage;

			logger.log(TAG, "Asking server for registration details");
			response = server.contactServer(new RegistrationMessage(currentStageValue));
			logger.log(TAG, "Server response: " + response.getREST());

			if (response.isError()) {
				logger.log(TAG,
						"Bad server response. Sleeping for a short time.");
				newState = false;
				time = sleepTimeShort;
			} else {
				logger.log(TAG,
						"Requesting registration: " + response.getInfo());
				// Saves the new stage from the reply message
				if (response.getInfo() != null) {
					nextStage = tools.strToInt(response.getInfo());
				} else {
					break;
				}
				if (currentStageValue == nextStage) {
					logger.log(TAG, "currentStageValue == nextStage: "
							+ currentStageValue + "==" + nextStage);
					newState = false;
					// Just waiting to reg online
					if (currentStageValue < 2) {
						time = sleepTimeShort;
					} else {
						time = sleepTimeLong;
					}
				} else {
					logger.log(TAG, "currentStageValue != nextStage: "
							+ currentStageValue + "!=" + nextStage);
					newState = true;
					if (0 == currentStageValue) {
						logger.log(TAG, "currentStageValue = "
								+ currentStageValue);
						regID = response.getRegID();
						setRegData(KEY_ID, regID);
					}
					// assigns new stage
					regStage = nextStage;
					// Saves stage to memory
					setRegData(KEY_STAGE, String.valueOf(regStage));
					// Process stage
					stageState(regStage);
				}
			}

			if (!newState) {
				logger.log(TAG, "newState = true");
				try {
					logger.log(TAG, "Sleeping for " + time);
					Thread.sleep(time);// 1Day
					logger.log(TAG, "RegWalk");
				} catch (Exception e) {
					ActivityLog.addMessage(new ErrorMessage(e));
					break;
				}
			}
		}
	}

	/**
	 * Update the registration preferences info and commit to storage.
	 * 
	 * @param key
	 *            - the registration key. Use class constants.
	 * @param value
	 *            - the new value to commit.
	 * @return true if value was updated, false if value was created
	 */
	private boolean setRegData(String key, String value) {
		PersistentObject regData = PersistentStore
				.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			Object oldValue = regTable.put(key, value);
			if (oldValue == null) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Acts as a lookup for stages for the current device to display the
	 * registration stage to the user.
	 * 
	 * @param inputStage
	 *            - stage of registration.
	 */
	private void stageState(int inputStage) {
		MobileMinder app = (MobileMinder)UiApplication.getUiApplication();
		String stateText = "";

		switch (inputStage) {
		case 0: // New install
			stateText = r.getString(i18n_RegRequesting);
			break;
		case 1:// New & has SN
			stateText = r.getString(i18n_RegNotActivated);
			// TODO: put this into thread
			// tools.addMsgToInbox(r.getString(i18n_WelcomeMsg));
			break;
		case 2: // Trial
			stateText = r.getString(i18n_RegTrial);
			//registered = true;
			app.startComponents();
			break;
		case 3: // Fully active
			stateText = r.getString(i18n_RegActive);
			//registered = true;
			app.startComponents();
			break;
		}
		switchStage(inputStage, stateText);
	}

	private void switchStage(int inputStage, String stateText) {
		switch (inputStage) {
		case 0: // New install
			status = stateText;
			logger.log(TAG, "Status text updated to: " + status);
			break;
		case 1: // New & has SN
		case 2: // Trial
		case 3: // Fully active
			status = stateText;
			logger.log(TAG, "Status text updated to: " + status);
			logger.log(TAG, "Reg id:" + regID);
			break;
		}
		notifyObservers();
	}

	/**
	 * Get the device registration ID. Can be called from anywhere in the
	 * system.
	 * 
	 * @return registration ID string. <strong>"0"</strong> if not available.
	 */
	public static String getRegID() {
		if (regID == null) {
			return "0";
		} else {
			return regID;
		}
	}

	public static String getStatus() {
		return status;
	}

	/**
	 * Gets the emergency numbers associated with the account. Can be used
	 * anywhere in the system.
	 * 
	 * @return String array of emergency numbers.
	 */
	public static String[] getEmergNums() {
		return ToolsBB.getInstance().split(emergNums, "&");
	}

	public boolean processCommand(String[] inputArgs) {
		logger.log(TAG, "Processing Owner Number Command...");
		boolean complete = false;
		if (inputArgs[0].equalsIgnoreCase("lost")
				&& inputArgs[1].equalsIgnoreCase("number")) {

			logger.log(TAG, "args[0] :" + inputArgs[0]);
			logger.log(TAG, "args[1] :" + inputArgs[1]);
			logger.log(TAG, "args[2] :" + inputArgs[2]);
			try {
				emergNums = inputArgs[2];
				setRegData(KEY_NUMBERS, emergNums);
				complete = true;
			} catch (Exception e) {
				ActivityLog.addMessage(new ErrorMessage(e));
				complete = false;
			}
		}
		return complete;
	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.OWNER) {
			return true;
		} else {
			return false;
		}
	}

	public static void addObserver(Screen screen) {
		observers.addElement(screen);
	}

	public static void removeObserver(Screen screen) {
		observers.removeElement(screen);
	}

	private void notifyObservers() {
		for (int i = 0; i < observers.size(); i++) {
			((ObserverScreen) observers.elementAt(i)).update();
		}
	}
}

class RegistrationMessage implements Message {
	private final static int type = 9;
	private final String appVersion = ApplicationDescriptor.currentApplicationDescriptor().getVersion();
	private String deviceTime;
	private int stage;
	private String phoneNum;
	private String deviceID;
	private String info;
	private String manufacturer;
	private ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	public RegistrationMessage(int stage) {
		this.stage = stage;
		deviceTime = tools.getDate();
		manufacturer = String.valueOf(Branding.getVendorId());
		phoneNum = Phone.getDevicePhoneNumber(false);
		deviceID = Integer.toHexString(DeviceInfo.getDeviceId());
		info = "BlackBerry";
	}

	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + '0'
				+ type + Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + stage
				+ Tools.ServerQueryStringSeparator + phoneNum
				+ Tools.ServerQueryStringSeparator + deviceID
				+ Tools.ServerQueryStringSeparator + manufacturer
				+ Tools.ServerQueryStringSeparator + DeviceInfo.getDeviceName()
				+ Tools.ServerQueryStringSeparator
				+ DeviceInfo.getSoftwareVersion()
				+ Tools.ServerQueryStringSeparator + appVersion
				+ Tools.ServerQueryStringSeparator + info;
	}

	public String getTime() {
		return deviceTime;
	}

	public int getType() {
		return type;
	}
}