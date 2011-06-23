package com.kids;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;

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
	//ApplicationIndicator indicator;
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
}
