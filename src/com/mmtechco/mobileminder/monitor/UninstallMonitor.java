//#preprocess
//#implicit VER_5.0.0 | VER_6.0.0 | VER_7.0.0
package com.mmtechco.mobileminder.monitor;

import java.io.IOException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.CodeModuleListener;
import net.rim.device.api.ui.component.Dialog;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class UninstallMonitor implements CodeModuleListener, MobileMinderResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(UninstallMonitor.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	private static final int messageType = 33;
	
	private MMTools tools = ToolsBB.getInstance();

	public void modulesDeleted(String[] moduleNames) {
		Logger.log(TAG, "Modules deleted");

		for (int i = 0; i < moduleNames.length; i++) {
			if (moduleNames[i].equalsIgnoreCase(r.getString(i18n_AppName))) {
				Logger.log(TAG, "Sending Uninstall Notification to Server...");
				Response response;
				try {
					response = Server.get(Registration.getRegID() + "," + messageType + "," + tools.getDate() + "," + true);
					Reply reply = new Reply(response.getContent());
					if (reply.isError() == true) {
						Logger.log(TAG, "Error Sending Uninstall Notification");
					}
				} catch (IOException e) {
					Logger.log(TAG, e.getMessage());
				} catch (Exception e) {
					Logger.log(TAG, e.getMessage());
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
		Logger.log(TAG, "Modules added");
	}
}