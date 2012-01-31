//#preprocess
package com.mmtechco.mobileminder;

import java.io.IOException;

import com.mmtechco.mobileminder.prototypes.ObserverScreen;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

//#ifndef VER_4.5.0
import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.ui.decor.BackgroundFactory;
//#endif
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
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

public class InfoScreen extends MainScreen implements ObserverScreen,
		MobileMinderResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(InfoScreen.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private static Logger logger = Logger.getInstance();

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	
	//#ifndef VER_4.5.0
	// Notification icons
	ApplicationIcon icon_reg = new ApplicationIcon(
			EncodedImage.getEncodedImageResource("notify_reg.png"));
	ApplicationIcon icon_unreg = new ApplicationIcon(
			EncodedImage.getEncodedImageResource("notify_unreg.png"));
	ApplicationIcon notifyIcon = icon_unreg;
	//#endif

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
				String[] emergNums = Registration.getEmergNums();
				if (emergNums[0] != "" && emergNums.length > 0) {
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
		/*
		 * FieldChangeListener buttonListener = new FieldChangeListener() {
		 * public void fieldChanged(Field field, int context) {
		 * Dialog.alert("Success!!! You clicked the Custom Button!!!"); } };
		 * ImageButton helpButton = new ImageButton("icon_help.png",
		 * "notify_unreg.png"); helpButton.setChangeListener(buttonListener);
		 */
		vfm.add(helpButton);

		//#ifndef VER_4.5.0
		vfm.setBackground(BackgroundFactory.createSolidTransparentBackground(
				Color.GRAY, 50));
		//#endif
		add(vfm);
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

	public void close() {
		// App is pushed to background rather than terminated when screen is
		// closed.
		UiApplication.getUiApplication().requestBackground();
	}

	public boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
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
		String[] emergNums = Registration.getEmergNums();
		if (emergNums[0] != "" && emergNums.length > 0) {
			menu.add(helpMenu);
		}

		super.makeMenu(menu, instance);
	}

	private boolean sendHelpMe() {
		// TODO: implement location part
		String[] emergNums = Registration.getEmergNums();
		for (int i = 0; i < emergNums.length; i++) {
			try {
				// TODO: put message into resource bundle
				((ToolsBB) ToolsBB.getInstance()).sendSMS(emergNums[i],
						"help me");
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	//#ifndef VER_4.5.0
	public void registerIndicator() {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			ApplicationIndicator indicator = reg.register(notifyIcon, false,
					true);
		} catch (Exception e) {
			logger.log(TAG, "Could not register notification icon");
		}
	}

	public void unregisterIndicator() {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			reg.unregister();
		} catch (Exception e) {
			logger.log(TAG, "Could not unregister notification icon");
		}
	}

	public void updateValue(int value) {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			ApplicationIndicator appIndicator = reg.getApplicationIndicator();
			appIndicator.setValue(value);
		} catch (Exception e) {
			logger.log(TAG, "Could not update notification icon value");
		}
	}

	public void updateIcon(ApplicationIcon icon) {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			ApplicationIndicator appIndicator = reg.getApplicationIndicator();
			appIndicator.setIcon(icon);
		} catch (Exception e) {
			logger.log(TAG, "Could not update notification icon");
		}
	}
	//#endif

	// Custom private class that creates the button and switches the image
	// depending on the return value of onFocus()
	private class ImageButton extends Field implements DrawStyle {
		private Bitmap currentPicture;
		private Bitmap onPicture; // image for "in focus"
		private Bitmap offPicture; // image for "not in focus"
		private int width;
		private int height;

		ImageButton(String onImage, String offImage) {
			super();
			onPicture = Bitmap.getBitmapResource(onImage);
			offPicture = Bitmap.getBitmapResource(offImage);
			currentPicture = offPicture;
		}

		public int getPreferredHeight() {
			return 80;
		}

		public int getPreferredWidth() {
			return 80;
		}

		public boolean isFocusable() {
			return true;
		}

		protected void onFocus(int direction) {
			currentPicture = onPicture;
			invalidate();
		}

		protected void onUnfocus() {
			currentPicture = offPicture;
			invalidate();
		}

		protected void layout(int width, int height) {
			setExtent(Math.min(width, getPreferredWidth()),
					Math.min(height, getPreferredHeight()));
		}

		protected void fieldChangeNotify(int context) {
			try {
				this.getChangeListener().fieldChanged(this, context);
			} catch (Exception e) {
				logger.log(TAG, e.getMessage());
			}
		}

		// Button is rounded so fill in edges with colors to match screen
		// background
		protected void paint(Graphics graphics) {
			graphics.setColor(Color.GRAY);
			// graphics.setGlobalAlpha(50);
			graphics.fillRect(0, 0, getWidth(), getHeight());
			graphics.drawBitmap(0, 0, getWidth(), getHeight(), currentPicture,
					0, 0);
		}

		protected boolean navigationClick(int status, int time) {
			fieldChangeNotify(1);
			return true;
		}
	}
}
