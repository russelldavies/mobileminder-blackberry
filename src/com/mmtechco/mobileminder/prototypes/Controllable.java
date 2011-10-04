package com.mmtechco.mobileminder.prototypes;

import com.mmtechco.mobileminder.prototypes.enums.COMMAND_TARGETS;

/**
 * An interface which defines the structure for processing commands and checking targets.
 *
 */
public interface Controllable {
	/**
	 * Receive command arguments and process them.
	 * @param args - arguments sent specifying the command
	 * instructions from the server.
	 * @return true if the command has been processed without
	 * any errors.
	 */
	public boolean processCommand(String[] args);

	/**
	 * Specifies the type of commands it that can be processed.
	 * Checks whether the target matches desired command type.
	 * @param targets - passed to be checked.
	 * @return true if this is the desired target.
	 */
	public boolean isTarget(COMMAND_TARGETS targets);
}
