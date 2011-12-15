package com.mmtechco.mobileminder;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.mmtechco.mobileminder.data.DbAdapter;
import com.mmtechco.mobileminder.prototypes.ObserverScreen;
import com.mmtechco.mobileminder.util.Constants;
import com.mmtechco.mobileminder.util.Logger;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.StringProvider;

/**
 * Screen that displays all the logging info. Useful for debugging. Enable by
 * setting the global debug flag in {@link Constants#DEBUG}
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
				add(new LabelField(msg));
			}
		});
	}

	protected void makeMenu(Menu menu, int instance) {
		MenuItem clearMenu = new MenuItem(new StringProvider("Clear Screen"),
				0x100020, 1) {
			public void run() {
				deleteAll();
			}
		};

		MenuItem delRegMenu = new MenuItem(new StringProvider(
				"Delete Registration info"), 0x100030, 2) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(
						new RegPopupScreen());
				PersistentStore.destroyPersistentObject(Constants.regData);
			}
		};

		MenuItem delDbMenu = new MenuItem(
				new StringProvider("Delete Database"), 0x100040, 3) {
			public void run() {
				try {
					FileConnection fc = (FileConnection) Connector
							.open(DbAdapter.dbLocation);
					fc.delete();
					fc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}

		};
		menu.add(clearMenu);
		menu.add(delRegMenu);
		menu.add(delDbMenu);

		super.makeMenu(menu, instance);
	}

	public void close() {
		RuntimeStore.getRuntimeStore().remove(DbAdapter.DB_ID);
		// TODO: call DBAccess.close, remove filesystem listener, remove call
		// and sms listeners.
		super.close();
	}

	final class RegPopupScreen extends PopupScreen {

		public RegPopupScreen() {
			super(new VerticalFieldManager());

			add(new LabelField("Registration Information"));
			add(new SeparatorField());

			PersistentObject regData = PersistentStore
					.getPersistentObject(Constants.regData);
			synchronized (regData) {
				Hashtable regTable = (Hashtable) regData.getContents();
				if (regTable == null) {
					add(new LabelField("No values were in the store"));
				} else {
					add(new LabelField(regTable.get(Registration.KEY_STAGE)));
					add(new LabelField(regTable.get(Registration.KEY_ID)));
					String num = (String) regTable.get(Registration.KEY_NUMBERS);
					if (num.equals("")) {
						add(new LabelField("No emergency numbers stored"));
					} else {
						add(new LabelField(num));
					}
				}
			}
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