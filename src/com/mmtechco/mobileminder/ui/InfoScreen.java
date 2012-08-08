package com.mmtechco.mobileminder.ui;

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

import com.mmtechco.mobileminder.MobileMinderResource;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.command.EmergencyNumbers;
import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.util.ToolsBB;

public class InfoScreen extends MainScreen implements ObserverScreen,
		MobileMinderResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	public InfoScreen() {
		super(NO_VERTICAL_SCROLL | USE_ALL_HEIGHT | USE_ALL_WIDTH);

		// Give reference of self to Registration so fields can be updated
		Registration.addObserver(this);

		// Set initial text for registration info fields
		statusTextField.setLabel("Status: ");
		statusTextField.setText(r.getString(i18n_RegRequesting));
		idTextField.setLabel("ID: ");
		idTextField.setText("[none]");

		// Define layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_HEIGHT
				| USE_ALL_WIDTH | FIELD_HCENTER);

		// Logo
		Bitmap logoBitmap = Bitmap.getBitmapResource("mobileminder.png");
		float ratio = (float) logoBitmap.getWidth() / logoBitmap.getHeight();
		int newWidth = (int) (Display.getWidth() * 0.9);
		int newHeight = (int) (newWidth / ratio);
		BitmapField logoField = new BitmapField(ToolsBB.resizeBitmap( logoBitmap, newWidth, newHeight, Bitmap.FILTER_LANCZOS, Bitmap.SCALE_TO_FIT), Field.FIELD_HCENTER);
		logoField.setPadding(0, 0, 10, 0);
		vfm.add(logoField);

		// Registration fields
		vfm.add(statusTextField);
		vfm.add(idTextField);
		vfm.add(new SeparatorField());

		// Help Me button
		ButtonField helpButton = new ButtonField("Help Me!",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		helpButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				EmergencyNumbers.sendHelpMe();
			}
		});
		setStatus(helpButton);

		setBackground(BackgroundFactory.createSolidTransparentBackground(
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
		MenuItem helpMenu = new MenuItem(r.getString(i18n_MenuHelp), 0x100010,
				0) {
			public void run() {
				EmergencyNumbers.sendHelpMe();
			}
		};
		menu.add(helpMenu);

		super.makeMenu(menu, instance);
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
			ApplicationIndicatorRegistry.getInstance().register(icon_unreg,
					true, true);
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not register notification icon", e));
		}
	}

	public void updateIcon(ApplicationIcon icon) {
		try {
			ApplicationIndicatorRegistry.getInstance()
					.getApplicationIndicator().setIcon(icon);
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not update notification icon", e));
		}
	}

	public void unregisterIndicator() {
		try {
			ApplicationIndicatorRegistry.getInstance().unregister();
		} catch (RuntimeException e) {
			ActivityLog.addMessage(new ErrorMessage(
					"Could not unregister notification icon", e));
		}
	}
}
