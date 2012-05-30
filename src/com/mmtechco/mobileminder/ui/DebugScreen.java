package com.mmtechco.mobileminder.ui;

import java.util.Hashtable;
import java.util.Vector;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.data.FileLog;
import com.mmtechco.mobileminder.prototypes.ObserverScreen;
import com.mmtechco.util.Logger;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

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
			}
		};

		MenuItem delStoreMenu = new MenuItem("Delete Store", 0x100040, 3) {
			public void run() {
				PersistentStore.destroyPersistentObject(ActivityLog.ID);
				PersistentStore.destroyPersistentObject(FileLog.ID);
				System.exit(0);
			}

		};
		menu.add(clearMenu);
		menu.add(delRegMenu);
		menu.add(delStoreMenu);

		super.makeMenu(menu, instance);
	}

	public void close() {
		// TODO: remove filesystem listener, remove call and sms listeners.
		super.close();
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
					Vector nums = (Vector) regTable.get(Registration.KEY_NUMBERS);
					
					add(new LabelField("Stage: " + stage));
					add(new LabelField("ID: " + id.toString()));
					add(new LabelField("Components started: " + compStatus));
					add(new LabelField("Emergency nums: " + nums));
				}
			}

			ButtonField exitButton = new ButtonField("Exit",
					ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			exitButton.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					// Delete details
					PersistentStore.destroyPersistentObject(Registration.ID);
					RuntimeStore.getRuntimeStore().remove(Registration.ID);
					System.exit(0);
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