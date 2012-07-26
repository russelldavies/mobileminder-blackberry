//#preprocess
//#implicit VER_5.0.0 | VER_6.0.0 | VER_7.0.0
package com.mmtechco.mobileminder.monitor;

import java.io.IOException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.CodeModuleListener;
import net.rim.device.api.ui.component.Dialog;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.HttpClient;
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

public class UninstallMonitor implements CodeModuleListener,
		MobileMinderResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private static Logger logger = Logger.getLogger(UninstallMonitor.class);

	private MMTools tools = ToolsBB.getInstance();

	public void modulesDeleted(String[] moduleNames) {
		logger.debug("Modules deleted");

		for (int i = 0; i < moduleNames.length; i++) {
			if (moduleNames[i].equalsIgnoreCase(r.getString(i18n_AppName))) {
				logger.debug("Sending Uninstall Notification to Server...");
				try {
					Response response = HttpClient.get(Registration.getRegID()
							+ "," + Message.UNINSTALL + "," + tools.getDate() + ","
							+ true);
					Reply.Regular reply = new Reply.Regular(
							response.getContent());
					if (reply.error == true) {
						logger.debug("Error Sending Uninstall Notification");
					}
				} catch (IOException e) {
					logger.warn("Connection problem: " + e.getMessage());
				} catch (ParseException e) {
					ActivityLog.addMessage(new ErrorMessage(e));
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
		logger.debug("Modules added");
	}
}