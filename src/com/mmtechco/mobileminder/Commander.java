package com.mmtechco.mobileminder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Responsible for contacting the server and getting responses. It is also
 * responsible for processing responses.
 */
public class Commander {
	private static final String TAG = ToolsBB.getSimpleClassName(Commander.class);

	private static final int time = 1000 * 60 * 5; // 5 mins
	private static final int commandSignal = 0;
	
	private static Controllable components[];
	
	private static MMTools tools = ToolsBB.getInstance();

	public Commander(Controllable[] components) {
		Commander.components = components;
		new Timer().schedule(new CommandTask(), time);
		
	}

	/**
	 * Contacts to server and gets reply from server. Process reply if valid.
	 */
	private class CommandTask extends TimerTask {
		public void run() {
			while (true) {
				Response response;
				Reply reply;
				try {
					response = Server.get(new CommandMessage().toString());
					reply = new Reply(response.getContent());
				//} catch (IOException e) {
				} catch (Exception e) {
					Logger.log(TAG, e.getMessage());
					return;
				}
				
				//Logger.log(TAG, "Reply Index:" + reply.getIndex());

				// No more commands to process
				if (commandSignal == reply.getIndex() || reply.isError()) {
					Logger.log(TAG, "No Commands to Process");
					break;
				}
				
				for (int count = 0; count < components.length; count++) {
					CommandMessage commandMessage = new CommandMessage();
					Controllable aTarget = components[count];
					if (aTarget.isTarget(reply.getTarget())) {
						commandMessage.setStartTime();
						if (aTarget.processCommand(reply.getArgs())) {
							// Ran fine
							commandMessage.setEndTime();
							commandMessage.setMessage(reply.getIndex(), true);
							Logger.log(TAG, "Ran fine. Sending Command Reply Message: Index="
											+ reply.getIndex() + " REST:"
											+ commandMessage);
							//server.contactServer(commandMessage);
						} else {
							Logger.log(TAG, "No joy. Sending Command Reply. Index:" + reply.getIndex());
							commandMessage.setEndTime();
							commandMessage.setMessage(reply.getIndex(), false);
							//server.contactServer(commandMessage);
						}
						Logger.log(TAG, "Out of command reply= clearing data. Index:" + reply.getIndex());
					}
				}
			}
		}
	}

	class CommandMessage {
		private final int type = 0;
		
		private int index = 0;
		private boolean completed = false;
		private String startTime = "0";
		private String endTime = "0";

		/**
		 * Adds command message information to command message.
		 * 
		 * @param index
		 *            index of command message
		 * @param completed
		 *            sets true if the command reply ran fine, false otherwise.
		 */
		public void setMessage(int index, boolean completed) {
			this.index = index;
			this.completed = completed;
		}

		/**
		 * Retrieves the message formatted in to a single string value. Command
		 * message consists of:
		 * <ul>
		 * <li>Registration Serial number.
		 * <li>Command message type which is '0'.
		 * <li>Index of command message.
		 * <li>Boolean true if command message ran fine, false otherwise.
		 * <li>Start time of command message.
		 * <li>End time of command message.
		 * </ul>
		 * 
		 * @return a single string containing the entire message.
		 */
		public String toString() {
			return
					Registration.getRegID() + Server.separator +
					type + Server.separator +
					index + Server.separator +
					(completed ? 1 : 0) + Server.separator +
					startTime + Server.separator +
					endTime;
		}

		/**
		 * Sets the time when the command started to be processed.
		 */
		public void setStartTime() {
			startTime = tools.getDate();
		}

		/**
		 * Sets the current time when the command finished processing.
		 */
		public void setEndTime() {
			endTime = tools.getDate();
		}
	}
}
