package com.mmtechco.mobileminder;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Screen;

import com.mmtechco.mobileminder.data.DBFactory;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.MMServer;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;
import com.mmtechco.mobileminder.util.Constants;
import com.mmtechco.mobileminder.util.ErrorMessage;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.Tools;
import com.mmtechco.mobileminder.util.ToolsBB;

/**
 * Checks the registration stage that currently the device is in.
 */
public class Registration extends Thread implements Controllable, MobileMinderResource {
	private static final String TAG = ToolsBB.getSimpleClassName(Registration.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

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
	
	private PersistentObject regData;
	private Hashtable regTable;
	
	private boolean registered = false;
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
		regData = PersistentStore.getPersistentObject(Constants.regData);
		synchronized(regData) {
			regTable = (Hashtable) regData.getContents();
			if (regTable == null) {
				// Populate with default values
				regTable = new Hashtable();
				regTable.put(KEY_STAGE, "0");
				regTable.put(KEY_ID, "0");
				regTable.put(KEY_NUMBERS, "0");
				// Store
				regData.setContents(regTable);
				regData.commit();
			}
			regStage = Integer.parseInt(String.valueOf(regTable.get(KEY_STAGE)));
			regID = String.valueOf(regTable.get(KEY_ID));
			emergNums = String.valueOf(regTable.get(KEY_NUMBERS));
		}
		
		// Display help notification if registered and there are emergency
		// numbers.
		if (regStage >= 2 && emergNums != null) {
			// TODO: set HelpMe menu
		}
		stageState(regStage);
	}

	/**
	 * 
	 * Constantly checks the account status of the device at defined intervals.
	 * Checks if the SIM card of the device has been unlocked.
	 */
	public void run() {
		logger.log(TAG, "Now running");

		boolean newState = true;
		Reply response;
		int nextStage;
		int currentStageValue = regStage;
		int time = 0;

		// Try to get the phone number 10 times
		for (int tries = 0; Phone.getDevicePhoneNumber(false).equals("UNKNOWN NUMBER") && tries < 10; tries++) {
			logger.log(TAG, "Waiting for phone number");
			// set the registration notification text
			switchStage(0, r.getString(i18n_ErrorNoSim));
			try {
				Thread.sleep(sleepTimeShort);
			} catch (InterruptedException e) {
				break;
			}
		}
		stageState(regStage); 
		logger.log(TAG, "Registration stage: " + regStage);

		while (regStage < 2) {
			currentStageValue = regStage;
			
			logger.log(TAG, "Asking server for registration details");
			response = requestNextStage(currentStageValue);
			logger.log(TAG, "Server response: " + response.getREST());

			if (response.isError()) {
				logger.log(TAG, "Bad server response. Sleeping for a short time.");
				newState = false;
				time = sleepTimeShort;
			} else {
				logger.log(TAG, "Requesting registration: " + response.getInfo());
				// Saves the new stage from the reply message
				nextStage = tools.strToInt(response.getInfo());
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
					try {
						DBFactory.getLocalDataWriter().addMessage(new ErrorMessage(e));
					} catch (IOException e1) {
						logger.log(TAG, "No usable storage for DB.");
					}
					break;
				}
			}
		}
		stageState(regStage); 
	}

	/**
	 * Update the registration preferences info and commit to storage.
	 * 
	 * @param key
	 *            - the registration key. Use class constants.
	 * @param value
	 *            - the new value to commit.
	 * @return true if commit successful.
	 */
	private boolean setRegData(String key, String value) {
		Object oldValue = regTable.put(key, value);
		if (oldValue == null) {
			return false;
		}
		return true;
	}

	/**
	 * Acts as a lookup for stages for the current device to display the
	 * registration stage to the user.
	 * 
	 * @param inputStage
	 *            - stage of registration.
	 */
	private void stageState(int inputStage) {
		String stateText = "";

		switch (inputStage) {
		case 0: // New install
			stateText = r.getString(i18n_RegRequesting);
			break;
		case 1:// New & has SN
			stateText = r.getString(i18n_RegNotActivated);
			tools.addMsgToInbox(r.getString(i18n_WelcomeMsg));
			break;
		case 2: // Wed Reg
			stateText = r.getString(i18n_RegTrial);
			registered = true;
			break;
		case 3: // Device Reg
			stateText = r.getString(i18n_RegActive);
			registered = true;
			break;
		}
		switchStage(inputStage, stateText);
	}

	private void switchStage(int inputStage, String stateText) {
		switch (inputStage) {
		case 0: // New install
			logger.log(TAG, "Status text updated to: " + stateText);
			status = stateText;
			notifyObservers();
			break;
		case 1: // New & has SN
		case 2: // Wed Reg
		case 3: // Device Reg
			logger.log(TAG, "Status text updated to: " + stateText + " " + regID);
			status = stateText;
			notifyObservers();
			break;
		}
	}

	/**
	 * Sends a request to the server with device information and gets a return
	 * reply.
	 * 
	 * @param currentStage
	 *            - current registration stage of the device.
	 * @return a Reply message which contains the registration stage of the
	 *         account.
	 */
	private Reply requestNextStage(int currentStage) {
		logger.log(TAG, "requestNextStage1");
		Reply result = server.contactServer(new RegistrationMessage(
				currentStage, Phone.getDevicePhoneNumber(false), String.valueOf(DeviceInfo.getDeviceId()), tools
						.getDeviceManufacturer()));
		logger.log(TAG, "requestNextStage2");
		return result;
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
					// TODO: implement this
					//setSOSNotification();
				} catch (Exception e) {
					try {
						DBFactory.getLocalDataWriter().addMessage(new ErrorMessage(e));
					} catch (IOException e1) {
						logger.log(TAG, "No usable storage for DB.");
					}
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
	
	public boolean isRegistered() {
		return registered;
	}
	
	public static void addObserver(Screen screen) {
		observers.addElement(screen);
	}
	
	public static void removeObserver(Screen screen) {
		observers.removeElement(screen);
	}
	
	private void notifyObservers() {
		for(int i = 0; i < observers.size(); i++) {
			((InfoScreen) observers.elementAt(i)).update();
		}
	}
}

class RegistrationMessage implements Message {
	private final static int type = 9;
	private final int mmVERSION = 2;
	private boolean error;
	private String deviceTime;
	private int stage;
	private String phoneNum;
	private String deviceID;
	private String info;
	private String manufacturer;
	private ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	/**
	 * Sets up the values for RegistrationMessage.
	 * 
	 * @param inputStage
	 *            Current registration stage of the device.
	 * @param inputPhoneNum
	 *            Phone number of the device.
	 * @param inputDeviceID
	 *            Device identification number.
	 * @param inputManufacturer
	 *            Device manufacturer information.
	 */
	public RegistrationMessage(int inputStage, String inputPhoneNum,
			String inputDeviceID, String inputManufacturer) {
		error = false;
		deviceTime = tools.getDate();
		stage = inputStage;
		manufacturer = inputManufacturer;
		phoneNum = inputPhoneNum;
		deviceID = inputDeviceID;
		info = "Blackberry";
	}

	/**
	 * Retrieves error status of the RegistrationMessage.
	 * 
	 * @return error status.
	 */
	public boolean getError() {
		return error;
	}

	/**
	 * Retrieves information body of RegistrationMessage.
	 * 
	 * @return device type.
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * This method formats all the parameters of the RegistrationMessage into a
	 * single string.
	 * 
	 * @return It returns registration message as a single string.
	 */
	public String getREST() {
		return Registration.getRegID()
				+ Tools.ServerQueryStringSeparator
				+ '0'
				+ type
				+ Tools.ServerQueryStringSeparator
				+ // Server expects zero-prefixed type number
				deviceTime + Tools.ServerQueryStringSeparator + stage
				+ Tools.ServerQueryStringSeparator + phoneNum
				+ Tools.ServerQueryStringSeparator + deviceID
				+ Tools.ServerQueryStringSeparator + manufacturer
				+ Tools.ServerQueryStringSeparator + DeviceInfo.getDeviceName()
				+ Tools.ServerQueryStringSeparator + tools.getOSVersion()
				+ Tools.ServerQueryStringSeparator + mmVERSION
				+ Tools.ServerQueryStringSeparator + info;
	}

	/**
	 * Retrieves the time when the registrationMessage was created.
	 * 
	 * @return a string contains the time.
	 */
	public String getTime() {
		return deviceTime;
	}

	/**
	 * Retrieves the type of the registrationMessage type.
	 * 
	 * @return a string contains the type.
	 */
	public int getType() {
		return type;
	}
}