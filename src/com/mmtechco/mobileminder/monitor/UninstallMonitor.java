package com.mmtechco.mobileminder.monitor;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.CodeModuleListener;
import net.rim.device.api.ui.component.Dialog;

public class UninstallMonitor implements CodeModuleListener, MobileMinderResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(UninstallMonitor.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	private Logger logger = Logger.getInstance();

	public void modulesDeleted(String[] moduleNames) {
		logger.log(TAG, "Modules deleted");

		for (int i = 0; i < moduleNames.length; i++) {
			if (moduleNames[i].equalsIgnoreCase(r.getString(i18n_AppName))) {
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
