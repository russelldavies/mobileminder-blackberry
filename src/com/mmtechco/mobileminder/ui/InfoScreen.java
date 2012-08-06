package com.mmtechco.mobileminder.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.command.EmergencyNumbers;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.monitor.LocationMonitor;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.ToolsBB;

public class InfoScreen extends MainScreen implements ObserverScreen,
		MobileMinderResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	public InfoScreen() {
		super(Manager.NO_VERTICAL_SCROLL);

		// Give reference of self to Registration so fields can be updated
		Registration.addObserver(this);

		// Set initial text for registration info fields
		statusTextField.setLabel("Status: ");
		statusTextField.setText(r.getString(i18n_RegRequesting));
		idTextField.setLabel("ID: ");
		idTextField.setText("[none]");

		// Define layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(
				VerticalFieldManager.USE_ALL_HEIGHT
						| VerticalFieldManager.USE_ALL_WIDTH
						| VerticalFieldManager.FIELD_HCENTER);

		// Add logo
		vfm.add(new BitmapField(Bitmap.getBitmapResource("logo.png"),
				Field.FIELD_HCENTER));

		// Info blurb and icon
		HorizontalFieldManager info_hfm = new HorizontalFieldManager(
				HorizontalFieldManager.USE_ALL_WIDTH);
		info_hfm.add(new BitmapField(Bitmap.getBitmapResource("icon_72.png")));
		info_hfm.add(new LabelField(r.getString(i18n_Description)));
		info_hfm.setPadding(20, 0, 0, 0);
		vfm.add(info_hfm);
		vfm.add(new SeparatorField());

		// Registration fields
		vfm.add(statusTextField);
		vfm.add(idTextField);
		vfm.add(new SeparatorField());

		// Help Me button
		ButtonField helpButton = new ButtonField("Help Me!",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		helpButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (EmergencyNumbers.getNumbers().size() > 0) {
					if (sendHelpMe()) {
						Dialog.inform(r.getString(i18n_HelpSent));
					} else {
						Dialog.inform("Could not send help message");
					}
				} else {
					Dialog.inform("No emergency numbers have been set");
				}
			}
		});
		vfm.add(helpButton);

		vfm.setBackground(BackgroundFactory.createSolidTransparentBackground(
				Color.GRAY, 50));
		add(vfm);

		registerIndicator();
	}

	/*
	 * Update the screen label fields
	 */
	public void update() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				// Only set text if reg id has been received
				String regId = Registration.getRegID();
				if (!regId.equals("0")) {
					idTextField.setText(regId);
				}
				statusTextField.setText(Registration.getStatus());
				
				if (Registration.isRegistered()) {
					updateIcon(icon_reg);
				} else {
					updateIcon(icon_unreg);
				}
			}
		});
	}

	// Debugging: add log messages
	public void addNewLog(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				add(new LabelField(msg));
			}
		});
	}

	protected void makeMenu(Menu menu, int instance) {
		MenuItem helpMenu = new MenuItem(r.getString(i18n_MenuHelp), 0x100010, 0) {
			public void run() {
				// TODO: make modalless
				// Dialog.inform(r.getString(i18n_HelpSending));
				(new Thread() {
					boolean sendStatus = false;

					public void run() {
						sendStatus = sendHelpMe();
						if (sendStatus) {
							Dialog.inform(r.getString(i18n_HelpSent));
						}
					}
				}).start();
			}
		};

		// Only display menu if there are emergency numbers
		if (EmergencyNumbers.getNumbers().size() > 0) {
			menu.add(helpMenu);
		}

		super.makeMenu(menu, instance);
	}

	private boolean sendHelpMe() {
		String mapLocation = "http://www.mobileminder.net/findme.php?"
				+ LocationMonitor.latitude + "," + LocationMonitor.longitude;
		Vector emergNums = EmergencyNumbers.getNumbers();
		for (Enumeration nums = emergNums.elements(); nums.hasMoreElements();) {
			try {
				((ToolsBB) ToolsBB.getInstance()).sendSMS(
						(String) nums.nextElement(), r.getString(i18n_HelpMsg)
								+ mapLocation);
			} catch (IOException e) {
				ActivityLog.addMessage(new ErrorMessage(e));
				return false;
			}
		}
		return true;
	}
	
	public void close() {
		// App is pushed to background rather than terminated when screen is
		// closed.
		UiApplication.getUiApplication().requestBackground();
	}

	public boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
	}

	// Notification icons
	ApplicationIcon icon_reg = new ApplicationIcon(
			EncodedImage.getEncodedImageResource("notify_reg.png"));
	ApplicationIcon icon_unreg = new ApplicationIcon(
			EncodedImage.getEncodedImageResource("notify_unreg.png"));

	public void registerIndicator() {
		try {
			ApplicationIndicatorRegistry.getInstance().register(icon_unreg, true, true);
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage("Could not register notification icon", e));
		}
	}

	public void updateIcon(ApplicationIcon icon) {
		try {
			ApplicationIndicatorRegistry.getInstance().getApplicationIndicator().setIcon(icon);
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage("Could not update notification icon", e));
		}
	}
	
	public void unregisterIndicator() {
		try {
			ApplicationIndicatorRegistry.getInstance().unregister();
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage("Could not unregister notification icon", e));
		}
	}
}
