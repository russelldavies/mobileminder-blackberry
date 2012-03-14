package com.mmtechco.mobileminder.ui;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class BrowserScreen extends MainScreen {
	private BrowserField browser;
	private EditField url;
	private LabelField status;
	
	private BrowserFieldListener listener = new BrowserFieldListener() {
		private void updateStatus(final String message) {
			Application.getApplication().invokeLater(new Runnable() {
				public void run() {
					status.setText(message);
				}
			});
		}
			
		public void documentError(BrowserField browserField, Document document) {
			updateStatus("error  " + document.getDocumentURI());
		}
		
		public void documentLoaded(BrowserField browserField, Document document) {
			updateStatus("loaded " + document.getDocumentURI());
		}
		
		public void downloadProgress(BrowserField browserField, ContentReadEvent event) {
			updateStatus("downloaded " + event.getItemsRead() + " of " + event.getItemsToRead());
		}
	};

	public BrowserScreen() {
		setTitle("MobileMinder Web Browser");
		/*
		BrowserFieldConfig myBrowserFieldConfig = new BrowserFieldConfig();
        myBrowserFieldConfig.setProperty(BrowserFieldConfig.NAVIGATION_MODE,BrowserFieldConfig.NAVIGATION_MODE_POINTER);
        BrowserField browserField = new BrowserField(myBrowserFieldConfig);
        
        add(browserField);
        browserField.requestContent("http://www.blackberry.com");
        */
        
		/*
        BrowserFieldConfig config = new BrowserFieldConfig();
        HttpHeaders headers = new HttpHeaders();
        headers.addProperty(HttpHeaders.HEADER_CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_TEXT_HTML);
        headers.addProperty(HttpHeaders.HEADER_ACCEPT_CHARSET, "UTF-8");
        config.setProperty(BrowserFieldConfig.HTTP_HEADERS, headers);
        BrowserField contentField = new BrowserField(config);
        this.add(contentField); // add to screen
        contentField.requestContent("http://google.com;deviceside=true");
        */
		
        url = new EditField("URL: " , "http://", Integer.MAX_VALUE, EditField.FILTER_URL);        
        url.setCursorPosition(7);
		status = new LabelField("Loading" );
		browser = new BrowserField();
		browser.addListener(listener);
		
		add(url);
		add(browser);
		add(status);
	}

	protected boolean keyChar(char key, int status, int time) {
		if (getLeafFieldWithFocus() == url && key == Characters.ENTER) {
			Dialog.alert("clicked");
			return true; // Consume the key event
		} else {
			return super.keyChar(key, status, time);
		}
	}
	
	protected void onUiEngineAttached(boolean attached) {
		if (attached) {
			try {
				browser.requestContent("http://google.com");
			} catch (Exception e) {
				Dialog.inform("Failed to load");
			}
		}
	}
}
