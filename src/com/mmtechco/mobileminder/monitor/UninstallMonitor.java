//#preprocess
//#implicit VER_5.0.0 | VER_6.0.0 | VER_7.0.0
package com.mmtechco.mobileminder.monitor;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.CodeModuleListener;
import net.rim.device.api.ui.component.Dialog;

public class UninstallMonitor implements CodeModuleListener, MobileMinderResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(UninstallMonitor.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	private static final int messageType = 33;
	
	private Logger logger = Logger.getInstance();
	private MMTools tools = ToolsBB.getInstance();

	public void modulesDeleted(String[] moduleNames) {
		logger.log(TAG, "Modules deleted");

		for (int i = 0; i < moduleNames.length; i++) {
			if (moduleNames[i].equalsIgnoreCase(r.getString(i18n_AppName))) {
				logger.log(TAG, "Sending Uninstall Notification to Server...");
				Reply resultREST = new Server().contactServer(Registration.getRegID() + "," + messageType + "," + tools.getDate() + "," + true);
				if (resultREST.isError() == true) {
					logger.log(TAG, "Error Sending Uninstall Notification");
				}
				Dialog.inform(r.getString(i18n_Uninstall));
			}
		}
	}

	public void moduleDeletionsPending(String[] moduleNames) {
		// Pass on as don't need to distinguish between deletions and pending
		// deletions
		modulesDeleted(moduleNames);
	}

	public void modulesAdded(int[] handles) {
		logger.log(TAG, "Modules added");
	}
}