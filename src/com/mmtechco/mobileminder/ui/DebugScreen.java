package com.mmtechco.mobileminder.ui;

import java.util.Hashtable;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.command.EmergencyNumbers;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.data.FileLog;
import com.mmtechco.mobileminder.data.FileSync;
import com.mmtechco.mobileminder.monitor.CallMonitor;
import com.mmtechco.mobileminder.monitor.LocationMonitor;
import com.mmtechco.mobileminder.monitor.SMSMonitor;
import com.mmtechco.util.Logger;

/**
 * Screen that displays all the logging info. Useful for debugging. Enable by
 * enabling the DEBUG preprocessor directive.
 */
public class DebugScreen extends MainScreen implements ObserverScreen,
		MobileMinderResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	public DebugScreen() {
		Registration.addObserver(this);
		Logger.addObserver(this);

		// Add label fields with no layout managers
		statusTextField.setLabel("Status: ");
		statusTextField.setText(r.getString(i18n_RegRequesting));
		idTextField.setLabel("ID: ");
		idTextField.setText("[none]");

		add(statusTextField);
		add(idTextField);
		add(new SeparatorField());
	}

	/*
	 * Update the screen label fields
	 */
	public void update() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				// Leave label blank if reg id doesn't yet exist
				String regId = Registration.getRegID();
				if (!regId.equals("0")) {
					idTextField.setText(regId);
				}
				statusTextField.setText(Registration.getStatus());
			}
		});
	}

	/*
	 * Add a new log event to the screen
	 */
	public void addNewLog(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				add(new LabelField(msg, Field.FOCUSABLE));
			}
		});
	}

	protected void makeMenu(Menu menu, int instance) {
		MenuItem clearMenu = new MenuItem("Clear Screen",
				0x100020, 1) {
			public void run() {
				deleteAll();
			}
		};

		MenuItem delRegMenu = new MenuItem("Delete Registration info", 0x100030, 2) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(
						new RegPopupScreen());
				PersistentStore.destroyPersistentObject(Registration.ID);
				RuntimeStore.getRuntimeStore().remove(Registration.ID);
			}
		};

		MenuItem delStoreMenu = new MenuItem("Delete FileLog", 0x100040, 3) {
			public void run() {
				PersistentStore.destroyPersistentObject(ActivityLog.ID);
				PersistentStore.destroyPersistentObject(FileLog.ID);
				//System.exit(0);
				close();
			}
		};
		
		MenuItem emergnumsMenu = new MenuItem("View Emergency Numbers", 0x100040, 3) {
			public void run() {
				Dialog.inform(EmergencyNumbers.getNumbers().toString());
			}
		};
		
		MenuItem eventloggerMenu = new MenuItem("Event Logger", 0x100040, 3) {
			public void run() {
				if (!EventLogger.startEventLogViewer()) {
					Dialog.alert("Sorry, couldn't start Event Logger. Don't know why...");
				}
			}
		};
		
		menu.add(clearMenu);
		menu.add(delRegMenu);
		menu.add(delStoreMenu);
		menu.add(emergnumsMenu);
		menu.add(eventloggerMenu);

		super.makeMenu(menu, instance);
	}

	public void close() {
		cleanup();
		super.close();
	}
	
	public static void cleanup() {
		try {
			Logger logger = Logger.getLogger(DebugScreen.class);

			// Remove all listeners and clear components start flag
			Logger.getLogger(DebugScreen.class).info("Stopping listeners");
			SMSMonitor.stop();
			logger.debug("stopped sms monitor");
			CallMonitor.stop();
			logger.debug("stopped call monitor");
			LocationMonitor.stop();
			logger.debug("stopped location monitor");
			FileSync.stop();
			logger.debug("stopped file sync");
			Logger.getLogger(DebugScreen.class).info("Stopped all listeners");
			RuntimeStore.getRuntimeStore().remove(Registration.ID);
			logger.debug("Cleared start components flag");
		} catch (Exception e) {
		}
	}

	final class RegPopupScreen extends PopupScreen {
		public RegPopupScreen() {
			super(new VerticalFieldManager());

			add(new LabelField("Registration Information"));
			add(new SeparatorField());

			PersistentObject regData = PersistentStore
					.getPersistentObject(Registration.ID);
			synchronized (regData) {
				Hashtable regTable = (Hashtable) regData.getContents();
				if (regTable == null) {
					add(new LabelField("No values were in the store"));
				} else {
					String stage = (String) regTable.get(Registration.KEY_STAGE);
					String id = (String) regTable.get(Registration.KEY_ID);
					Boolean compStatus = (Boolean) RuntimeStore.getRuntimeStore().get(Registration.ID);
					
					add(new LabelField("Stage: " + stage));
					add(new LabelField("ID: " + id.toString()));
					add(new LabelField("Components started: " + compStatus));
				}
			}

			ButtonField exitButton = new ButtonField("Clear registration details",
					ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			exitButton.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					// Delete registration details
					PersistentStore.destroyPersistentObject(Registration.ID);
					DebugScreen.cleanup();
					close();
				}
			});
			add(exitButton);
		}

		/**
		 * Overrides the default implementation. Closes the popup screen when
		 * the Escape key is pressed.
		 * 
		 * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
		 */
		public boolean keyChar(char c, int status, int time) {
			if (c == Characters.ESCAPE) {
				close();
				return true;
			}

			return super.keyChar(c, status, time);
		}
	}
}