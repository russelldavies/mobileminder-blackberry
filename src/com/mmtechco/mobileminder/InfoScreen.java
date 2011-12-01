package com.mmtechco.mobileminder;

import java.io.IOException;
import java.util.Vector;

import com.mmtechco.mobileminder.util.Constants;
import com.mmtechco.mobileminder.util.Logger;
import com.mmtechco.mobileminder.util.ToolsBB;

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class InfoScreen extends MainScreen implements MobileMinderResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(InfoScreen.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	private static Logger logger = Logger.getInstance();

	// private StatusThread statusThread = new StatusThread();

	// GUI widgets
	private LabelField regStatusLabel = new LabelField(
			r.getString(i18n_RegRequesting));
	private LabelField regIDLabel = new LabelField();
	private ButtonField helpButton;

	// Notification icons
	ApplicationIcon icon_reg = new ApplicationIcon(
			EncodedImage.getEncodedImageResource(Constants.icon_notify_reg));
	ApplicationIcon icon_unreg = new ApplicationIcon(
			EncodedImage.getEncodedImageResource(Constants.icon_notify_unreg));
	ApplicationIcon notifyIcon = icon_unreg;

	// Menu items
	private MenuItem helpMenuItem = new MenuItem(r.getString(i18n_MenuHelp), 0,
			0) {
		public void run() {
			// TODO: make modalless
			Dialog.inform(r.getString(i18n_HelpSending));
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

	public InfoScreen() {
		// Allow elements to scroll off screen
		super(MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR);

		// Start helper thread
		// statusThread.start();

		// General layout manager
		VerticalFieldManager vfm = new VerticalFieldManager(
				VerticalFieldManager.USE_ALL_HEIGHT
						| VerticalFieldManager.USE_ALL_WIDTH
						| VerticalFieldManager.FIELD_HCENTER);
		// Add logo
		vfm.add(new BitmapField(Bitmap.getBitmapResource("logo.png"),
				Field.FIELD_HCENTER));

		// Information fields
		HorizontalFieldManager info_hfm = new HorizontalFieldManager(
				HorizontalFieldManager.USE_ALL_WIDTH);
		info_hfm.add(new BitmapField(Bitmap.getBitmapResource("icon_large.png")));
		info_hfm.add(new LabelField(r.getString(i18n_Description)));
		helpButton = new ButtonField("Help Me!", ButtonField.FIELD_HCENTER
				| ButtonField.CONSUME_CLICK);
		helpButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				// TODO: implement
				Dialog.inform("sending help");
			}
		});
		info_hfm.add(helpButton);
		info_hfm.setPadding(20, 0, 0, 0);
		vfm.add(info_hfm);

		vfm.add(new SeparatorField());

		// Registration fields
		HorizontalFieldManager reg_hfm = new HorizontalFieldManager();
		reg_hfm.add(regStatusLabel);
		regIDLabel.setFont(regIDLabel.getFont().derive(Font.BOLD));
		reg_hfm.add(regIDLabel);
		reg_hfm.setPadding(20, 0, 20, 0);
		vfm.add(reg_hfm);

		vfm.add(new SeparatorField());

		vfm.setBackground(BackgroundFactory.createSolidTransparentBackground(
				Color.GRAY, 50));
		add(vfm);
	}

	protected void onUiEngineAttached(boolean attached) {
		// Display help notification if registered and there are emergency
		// numbers.
		// if (regStage >= 2 && emergNums != "0") {
		setIdLabel(Registration.getRegID());
	}

	public boolean onClose() {
		UiApplication.getUiApplication().requestBackground();
		return true;
	}

	public void setStatusLabel(String text) {
		regStatusLabel.setText(text);
	}

	public void setIdLabel(String text) {
		regIDLabel.setText(text);
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(helpMenuItem);
		super.makeMenu(menu, instance);
	}

	private void registerIndicator() {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			ApplicationIndicator indicator = reg.register(notifyIcon, false,
					true);
		} catch (Exception e) {
			logger.log(TAG, "Could not register notification icon");
		}
	}

	private void unregisterIndicator() {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			reg.unregister();
		} catch (Exception e) {
			logger.log(TAG, "Could not unregister notification icon");
		}
	}

	void updateValue(int value) {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			ApplicationIndicator appIndicator = reg.getApplicationIndicator();
			appIndicator.setValue(value);
		} catch (Exception e) {
			logger.log(TAG, "Could not update notification icon value");
		}
	}

	void updateIcon(ApplicationIcon icon) {
		try {
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry
					.getInstance();
			ApplicationIndicator appIndicator = reg.getApplicationIndicator();
			appIndicator.setIcon(icon);
		} catch (Exception e) {
			logger.log(TAG, "Could not update notification icon");
		}
	}

	/*
	private void updateContent() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				doPaint();
			}
		});
	}

	/*
	 * private class StatusThread extends Thread { public void run() { for (;;)
	 * { //updateContent(new Date().toString()); updateContent(); try {
	 * sleep(1000); } catch (InterruptedException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } } } }
	 */

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
}
