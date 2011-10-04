package com.mmtechco.mobileminder.prototypes;

/**
 * An interface which defines the structure for processing currently running application notifications
 *
 */
public interface Hear {
	
	public boolean lookingFor(String appName);
	public boolean notifySubcriber(boolean start);
}
