package com.mmtechco.mobileminder.prototypes;
/**
 * This interface is used to receive device location changes from the MMLocationManager
 */
public interface MMLocationReceiver {

	public void OnLocationChange(MMLocationReceiver mmLocation);
	
}
