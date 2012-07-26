//#preprocess
//#implicit VER_5.0.0 | VER_6.0.0 | VER_7.0.0
package com.mmtechco.mobileminder.ui;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class BrowserScreen extends MainScreen {
	private BrowserField browserField;
	private EditField locationBar;
	private GaugeField progressBar;
	// The progress tracker object that updates the progress bar
	private BrowserFieldLoadProgressTracker progressTracker;
	
	// Menus
	MenuItem goMenu;
	MenuItem backMenu;
	MenuItem forwardMenu;
	
	WebMessage message;
	
	private final String INITIAL_URL = "http://www.google.com";
	
	public BrowserScreen() {
		super(Field.USE_ALL_WIDTH | Field.USE_ALL_HEIGHT | Manager.VERTICAL_SCROLLBAR | Manager.HORIZONTAL_SCROLLBAR);
		
		setTitle("MobileMinder Web Browser");
		
		// Setup properties for browser field
		BrowserFieldConfig browserFieldConfig = new BrowserFieldConfig();
		browserFieldConfig.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
        HttpHeaders headers = new HttpHeaders();
        headers.addProperty(HttpHeaders.HEADER_CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_TEXT_HTML);
        headers.addProperty(HttpHeaders.HEADER_ACCEPT_CHARSET, "UTF-8");
        browserFieldConfig.setProperty(BrowserFieldConfig.HTTP_HEADERS, headers);
        /*
        ConnectionFactory connectionFactory = new ConnectionFactory();
        int[] trasportTypes = {	TransportInfo.TRANSPORT_TCP_CELLULAR,
                               TransportInfo.TRANSPORT_TCP_WIFI};
        connectionFactory.setPreferredTransportTypes(trasportTypes);
        browserFieldConfig.setProperty(BrowserFieldConfig.CONNECTION_FACTORY, connectionFactory);
        */
		
        // Create browser field with configured properties
		browserField = new BrowserField(browserFieldConfig);
		browserField.addListener(new BrowserFieldListener() {
			public void downloadProgress(BrowserField browserField, final ContentReadEvent event) throws Exception {
				int value = (int) (100 * progressTracker .updateProgress(event));
				progressBar.setValue(value);
			}

			public void documentLoaded(final BrowserField browserField, Document document) throws Exception {
				progressTracker.reset();
				progressBar.setValue(100);
				// Update location bar to current url
				locationBar.setText(browserField.getDocumentUrl());
				// Send url to server
				if (message != null) {
					message.finished();
				}
				message = new WebMessage(browserField.getDocumentUrl(), browserField.getDocumentTitle());
			}
		});
		
        locationBar = new EditField("URL: " , "http://", Integer.MAX_VALUE, EditField.FILTER_URL);        
        locationBar.setCursorPosition(7);
		progressBar = new GaugeField("", 0, 100, 0, Field.USE_ALL_WIDTH);
		progressTracker = new BrowserFieldLoadProgressTracker(10f);
		
		add(locationBar);
		add(progressBar);
		add(new SeparatorField());
		add(browserField);
		
		// Menus
		goMenu = new MenuItem("Go", 0x100000, 0) {
			public void run() {
				browserField.requestContent(locationBar.getText().trim());
			}
		};
		backMenu = new MenuItem("Back", 0x110000, 0) {
			public void run() {
				browserField.back();
			}
		};
		goMenu = new MenuItem("Forward", 0x120000, 0) {
			public void run() {
				browserField.forward();
			}
		};
		addMenuItem(goMenu);
		addMenuItem(backMenu);
		addMenuItem(forwardMenu);
	}

	protected boolean keyChar(char key, int status, int time) {
		if (getLeafFieldWithFocus() == locationBar && key == Characters.ENTER) {
			// progressBar.reset("", 0, 100, 0);
			goMenu.run();
			return true;
		} else if (getLeafFieldWithFocus() == browserField && key == Characters.ESCAPE) {
			backMenu.run();
			return true;
		} else {
			return super.keyChar(key, status, time);
		}
	}
	
	protected boolean navigationClick(int status, int time) {
		if (getLeafFieldWithFocus() == locationBar) {
			goMenu.run();
			return true;
		}
		return super.navigationClick(status, time);
	}
	
	protected void onUiEngineAttached(boolean attached) {
		if (attached) {
			try {
				browserField.requestContent(INITIAL_URL);
			} catch (Exception e) {
				Dialog.inform("Failed to load");
			}
		}
	}
	
	public boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
	}
	
	public static void display() {
		// Bring up menu
		EventInjector
				.invokeEvent(new EventInjector.KeyCodeEvent(
						EventInjector.KeyCodeEvent.KEY_DOWN,
						(char) Keypad.KEY_MENU, 0));
		// Cycle down menu
		for (int i = 0; i < 20; i++) {
			EventInjector
					.invokeEvent(new EventInjector.NavigationEvent(
							EventInjector.NavigationEvent.NAVIGATION_MOVEMENT,
							0, 1, 0));
		}
		// Click on menu item
		EventInjector
				.invokeEvent(new EventInjector.KeyCodeEvent(
						EventInjector.KeyCodeEvent.KEY_DOWN,
						(char) Keypad.KEY_ENTER, 0));

		// Start custom browser
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				UiApplication.getUiApplication()
						.pushScreen(new BrowserScreen());
			}
		});
	}
}

/**
 * BrowserFieldLoadProgressTracker - Keeps track of browser field page-load
 * progress
 * 
 * Challenge: - Number of resources (e.g., images, css, javascript) to load is
 * not known beforehand
 * 
 */
class BrowserFieldLoadProgressTracker {

	// The fraction used to split the remaining load amount in the progress bar
	// after a new resource is found (e.g., 2=half, 3=one third, 5=one fifth)
	// Bar progress typically moves fast at the beginning and slows down
	// progressively as we don't know how many resources still need to be loaded
	private float progressFactor;

	// The percentage value left until the progress bar is fully filled out
	// (initial value=1 or 100%)
	private float percentageLeft;

	// Stores info about the resources being loaded
	private Hashtable resourcesTable; // Map: Resource (Connection) --->
										// ProgressTrackerEntry

	// Stores info about a resource being loaded
	static class ProgressTrackerEntry {
		ProgressTrackerEntry(int bytesRead, int bytesToRead, float percentage) {
			this.bytesRead = bytesRead;
			this.bytesToRead = bytesToRead;
			this.percentage = percentage;
		}
		
		// bytes read so far for this resource
		int bytesRead; 
		// total number of bytes that need to be read for this resource
		int bytesToRead;
		// the amount (in percentage) this resource represents in the progress
		// bar (e.g., 50%, 25%, 12.5%, 6.25%)
		float percentage;

		public void updateBytesRead(int bytesRead) {
			bytesRead += bytesRead;
			if (bytesRead > bytesToRead) {
				// this can happen when the final size of a resource cannot be
				// anticipated
				bytesToRead = bytesRead;
			}
		}
	}

	public BrowserFieldLoadProgressTracker(float progressFactor) {
		this.progressFactor = progressFactor;
		reset();
	}

	public synchronized void reset() {
		resourcesTable = null;
		percentageLeft = 1f;
	}

	public synchronized float updateProgress(ContentReadEvent event) {
		if (resourcesTable == null) {
			resourcesTable = new Hashtable();
		}
		Object resourceBeingLoaded = event.getSource();
		ProgressTrackerEntry entry = (ProgressTrackerEntry) resourcesTable
				.get(resourceBeingLoaded);
		if (entry == null) {
			float progressPercentage = percentageLeft / progressFactor;
			percentageLeft -= progressPercentage;
			resourcesTable.put(resourceBeingLoaded, new ProgressTrackerEntry(
					event.getItemsRead(), event.getItemsToRead(),
					progressPercentage));
		} else {
			entry.updateBytesRead(event.getItemsRead());
		}
		return getProgressPercentage();
	}

	/**
	 * Returns the amount of items read so far in percentage so that the
	 * progress bar can be updated.
	 * 
	 * @return the amount of items read so far in percentage (0.0-1.0)
	 */
	public synchronized float getProgressPercentage() {
		float percentage = 0f;
		for (Enumeration e = resourcesTable.elements(); e.hasMoreElements();) {
			ProgressTrackerEntry entry = (ProgressTrackerEntry) e.nextElement();
			percentage += ((entry.bytesRead / entry.bytesToRead) * entry.percentage);
		}
		return percentage;
	}
}

class WebMessage extends Message {
	private Date startTime;
	private String url, pageTitle;
	
	/**
	 * Message format:
	 * <ul>
	 * <li>Device time
	 * <li>Time page was viewed
	 * <li>Page title
	 * <li>Page URL
	 * </ul>
	 */
	public WebMessage(String url, String pageTitle) {
		super(Message.WEB_HISTORY);
		add(ToolsBB.getInstance().getDate());
		startTime = new Date();
		this.url = url;
		this.pageTitle = pageTitle;
	}
	
	public void finished() {
		int viewTime = (int) (new Date().getTime() - startTime.getTime()) / 1000;
		add(String.valueOf(viewTime));
		add(pageTitle);
		add(url);
		
		ActivityLog.addMessage(this);
	}
}