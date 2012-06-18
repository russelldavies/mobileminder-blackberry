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

	private static final String TYPE = "LOST";
	private static final String VERB = "NUMBER";

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

	public boolean processCommand(String[] args) {
		logger.info("Processing emergency number command");
		try {
			String type = args[0];
			String verb = args[1];
			String[] nums = ToolsBB.getInstance().split(args[2], "&");

			// Check type and verb are matching
			if (!(type.equalsIgnoreCase(TYPE) && verb.equalsIgnoreCase(VERB))) {
				return false;
			}

			for (int i = 0; i < nums.length; i++) {
				numbers.addElement(nums[i]);
			}
			// Store details
			store.setContents(numbers);
			store.commit();

			return true;
		} catch (IndexOutOfBoundsException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not parse command args", e));
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not process emergency number list", e));
		}
		return false;
	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.OWNER) {
			return true;
		}
		return false;
	}
}
