package com.kids;

//import com.kids.prototypes.enums.COMMAND_TARGETS;
/**
 * An interface which defines the structure for processing commands and checking targets.
 *
 */
public interface Controllable
{
	public boolean processCommand(String[] args);
	//public boolean isTarget(COMMAND_TARGETS targets);
	public boolean isTarget(String targets);
}
