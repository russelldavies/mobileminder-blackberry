package com.mmtechco.mobileminder.command;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.monitor.LocationMonitor;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class EmergencyNumbers implements Controllable, MobileMinderResource  {
	public static final long ID = StringUtilities
			.stringHashToLong(EmergencyNumbers.class.getName());
	private static Logger logger = Logger.getLogger(EmergencyNumbers.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

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

			numbers.removeAllElements();
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

	public boolean isTarget(String target) {
		if (target.equalsIgnoreCase(Commander.TARGET.OWNER)) {
			return true;
		}
		return false;
	}
	
	public static void sendHelpMe() {
		if (getNumbers().size() < 1) {
			Dialog.inform("No emergency numbers have been set");
			return;
		}

		new Thread() {
			public void run() {
				String mapLocation = "http://www.mobileminder.net/findme.php?"
						+ LocationMonitor.latitude + ","
						+ LocationMonitor.longitude;
				for (Enumeration nums = EmergencyNumbers.getNumbers()
						.elements(); nums.hasMoreElements();) {
					try {
						String number = (String) nums.nextElement();
						ToolsBB.sendSMS(number, r.getString(i18n_HelpMsg)
								+ mapLocation);
					} catch (IOException e) {
						Dialog.inform("Could not send help message");
						ActivityLog.addMessage(new ErrorMessage(e));
						return;
					}
				}
				Dialog.inform(r.getString(i18n_HelpSent));
			}
		}.start();
	}
}
