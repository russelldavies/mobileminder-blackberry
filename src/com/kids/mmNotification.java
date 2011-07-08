package com.kids;

import java.util.Date;

import com.kids.prototypes.Debug;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.system.EncodedImage;


/**
 * mmNotification class sets up and displays an icon in the notification banner.
 * The icon has 2 variables associated with it:1-The icon, and 2-the numeric value to appear beside it
 * To display a default icon, call setupIcon(). To modify these values, call setNumValue() and changeIcon()
 * before calling setupIcon() again.
 * @author eoinzy
 *
 */
public class mmNotification
{
	static Debug logWriter = Logger.getInstance();

	ApplicationIndicatorRegistry reg;
	ApplicationIndicator appIndicator;
	ApplicationIcon icon;
	EncodedImage image;
	int iconValue;
	boolean iconWithValue = false;

	public mmNotification()
	{
		logWriter.log("mmNotification constructor");
		reg   		 = ApplicationIndicatorRegistry.getInstance();
		image 		 = EncodedImage.getEncodedImageResource("mmicon.png" );
		icon  		 = new ApplicationIcon( image );
		iconValue    = 0;
		setupIcon();
	}
	
	/**
	 * setupIcon will setup everything needed for the app to appear in the notification banner.
	 * Default settings will be used if changeIcon() and setNumValue() havent been used.
	 */
	public void setupIcon()
	{
		logWriter.log("mmNotification::setupIcon");
		//ApplicationIndicator indicator = reg.register( icon, true, true);
		// Do we need to create a new ApplicatinIndicator? Or just do this:
		reg.register( icon, iconWithValue, true);
		appIndicator = reg.getApplicationIndicator();

		// Set and display icon, and possibly a numeric value beside it
		if (iconWithValue)
			appIndicator.set( icon, iconValue );
		else
			appIndicator.setIcon(icon);
	}
	
	/**
	 * Set the numeric value to appear beside the notification icon
	 * @param numValue - The numeric value to appear beside the notificatin icon
	 */
	public void setNumValue(int numValue)
	{
		logWriter.log("mmNotification::setNumValue");
		iconValue=numValue;
	}

	/**
	 * Method for changing the icon. Pass the file name of the new icon
	 * @param iconPath The file name of the new icon
	 */
	public void changeIcon(String iconName)
	{
		logWriter.log("mmNotification::changeIcon");
		image = EncodedImage.getEncodedImageResource(iconName);
		icon = new ApplicationIcon( image );
	}
	
	/**
	 * Method either displays or hides the notification icon, depending on what boolean value is passed 
	 * @param iconVisibility - TRUE displays icon, FALSE hides icon
	 */
	public void toggleIconVisibility(boolean iconVisibility)
	{
		logWriter.log("mmNotification::toggleIconVisibility");
		appIndicator.setVisible(iconVisibility);
	}
	
	public void updateIcon()
	{
		reg.unregister();
		setupIcon();
	}
	
	public void set(boolean withNum)
	{
		iconWithValue = withNum;
	}
	
	/**
	 * Adds a message to the global inbox, in this case, containing registration info for the client.
	 * @param _message Status message to display in SMS
	 * @param _inputStage The registration stage the user is currently at
	 * @param _regID Registration ID of the device
	 */
	public static void addMsgToInbox(String _message, int _inputStage, String _regID)
	{
		StringBuffer theMessage = new StringBuffer();
		theMessage.append(_message);
		if (0 != _inputStage)
		{
			theMessage.append("\nYour unique Mobile Minder serial number is: ");
			theMessage.append(_regID);
			// We don't want to tell the user to register if they're at stage 3 or 0!
			//if (3 > _inputStage && 0 < _inputStage)	
			if (1 == _inputStage || 2 == _inputStage)
			{   // Enter in here only if _inputStage is 1 or 2
				theMessage.append("\nPlease log on to www.mobileminder.com and enter the serial number to register your blackbery!");
			}
		}
		
		try
		{ 
        	 Address _fromAddress = new Address("Mobile Minder","Mobile Minder");
        	 Session session = Session.waitForDefaultSession();
         	//Get list of folders
             Store store = session.getStore();  
             Folder[] folders = store.list(Folder.INBOX);  
             // We only retrieve one folder, so its element[0]
             Folder inbox = folders[0];  
   
             Message msg = new Message(inbox);  
             msg.setContent(theMessage.toString());
             msg.setFrom(_fromAddress);  
             msg.setStatus(Message.Status.RX_RECEIVED, Message.Status.RX_RECEIVED);  
             msg.setSentDate(new Date(System.currentTimeMillis()));  
             msg.setFlag(Message.Flag.REPLY_ALLOWED, true);  
             msg.setInbound(true);  
             msg.setSubject("Mobile Minder Registration Info");  
             inbox.appendMessage(msg);
		}
		catch (AddressException e)
		{
			logWriter.log("mmNotification::addMsgToInbox::AddressException::"+e.getMessage());
			e.printStackTrace();
		}
		catch (NoSuchServiceException e)
		{
			logWriter.log("mmNotification::addMsgToInbox::NoSuchServiceException::"+e.getMessage());
			e.printStackTrace();
		}  
		catch (MessagingException e)
		{
			logWriter.log("mmNotification::addMsgToInbox::MessagingException::"+e.getMessage());
			e.printStackTrace();
		}  		
	}	// end addMessageToInbox


} // end mmNotification class
