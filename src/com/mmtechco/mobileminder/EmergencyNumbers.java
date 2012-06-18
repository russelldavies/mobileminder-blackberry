package com.mmtechco.mobileminder;

import java.util.Vector;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.prototypes.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class EmergencyNumbers implements Controllable {
	public static final long ID = StringUtilities
			.stringHashToLong(EmergencyNumbers.class.getName());
	private static Logger logger = Logger.getLogger(EmergencyNumbers.class);

	private static PersistentObject store;
	private static ContentProtectedVector numbers;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			numbers = new ContentProtectedVector();
			store.setContents(numbers);
		}
		numbers = (ContentProtectedVector) store.getContents();
	}

	public EmergencyNumbers() {
		logger.debug("Emergency numbers: " + numbers);
		// regTable.put(KEY_NUMBERS, emergNums = new Vector());
	}

	/**
	 * Gets the emergency numbers associated with the account.
	 * 
	 * @return Vector with each element a string containing a number
	 */
	public static Vector getNumbers() {
		return numbers;
	}

	public boolean processCommand(String[] inputArgs) {
		logger.info("Processing Owner Number Command...");
		boolean complete = false;
		if (inputArgs[0].equalsIgnoreCase("lost")
				&& inputArgs[1].equalsIgnoreCase("number")) {

			logger.debug("args[0] :" + inputArgs[0]);
			logger.debug("args[1] :" + inputArgs[1]);
			logger.debug("args[2] :" + inputArgs[2]);
			try {
				String[] nums = ToolsBB.getInstance().split(inputArgs[2], "&");
				for (int i = 0; i < nums.length; i++) {
					numbers.addElement(nums[i]);
				}
				// Store details
				store.setContents(numbers);
				store.commit();
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
}
