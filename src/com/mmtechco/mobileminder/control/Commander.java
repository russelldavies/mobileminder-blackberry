package com.mmtechco.mobileminder.control;

import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.mobileminder.prototypes.Controllable;
import com.mmtechco.mobileminder.prototypes.LocalDataWriter;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.mobileminder.prototypes.Message;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

/**
 * Responsible for contacting the server and getting responses. It is also
 * responsible for processing responses.
 */
public class Commander extends Thread {
	private static final String TAG = "Commander";

	private Server server;
	private Controllable componentList[];
	private final int time;
	private LocalDataWriter actLog;
	private final int commandSignal = 0;
	private Logger logger = Logger.getInstance();
	private MMTools tools = ToolsBB.getInstance();

	/**
	 * Used by Controller to initialises the {@link LocalDataAccess} object and
	 * {@link Controllable} objects.
	 */
	public Commander(LocalDataWriter actlog,
			Controllable[] components) {
		this.actLog = actlog;
		server = new Server(actlog);
		time = 1000 * 60 * 5; // 5mins
		componentList = components;
	}

	/**
	 * Contacts to server and gets reply from server. Process reply if valid.
	 */
	public void run() {
		while (true) {
			boolean commandQueued = true;
			while (commandQueued && tools.isConnected()) {
				CommandMessage commandMessageFirst = new CommandMessage();
				commandMessageFirst.setMessage(commandSignal);

				// Sample reply. Enable for debugging:
				// Reply reply = new
				// Reply("12349,0,32,FILES,FILE_DEL_/sdcard/download/images.jpeg");
				Reply reply = server.contactServer(commandMessageFirst);
				logger.log(TAG, "Reply Index:" + reply.getIndex());

				// No more commands to process
				if (commandSignal == reply.getIndex() || reply.isError()) {
					commandQueued = false;
					logger.log(TAG, "No Commands to Process");
					break;
				} else {
					CommandMessage commandMessage = new CommandMessage();
					for (int count = 0; count < componentList.length; count++) {
						Controllable aTarget = componentList[count];
						if (aTarget.isTarget(reply.getTarget())) {
							commandMessage.setStartTime();
							if (aTarget.processCommand(reply.getArgs())) {
								// Ran fine
								commandMessage.setEndTime();
								commandMessage.setMessage(reply.getIndex(),
										true);
								logger.log(TAG,
										"Ran fine. Sending Command Reply Message: Index="
												+ reply.getIndex() + " REST:"
												+ commandMessage.getREST());
								server.contactServer(commandMessage);
							} else {
								logger.log(TAG,
										"No joy. Sending Command Reply. Index:"
												+ reply.getIndex());
								commandMessage.setEndTime();
								commandMessage.setMessage(reply.getIndex(),
										false);
								server.contactServer(commandMessage);
							}
							logger.log(TAG,
									"Out of command reply= clearing data. Index:"
											+ reply.getIndex());
							commandMessage.clearData();
						}
					}
				}
			}
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				actLog.addMessage(new ErrorMessage(e));
			}
		}
	}

	/**
	 * 
	 * Implements the message interface to hold command message.
	 */
	class CommandMessage implements Message {
		private final int type = 0;
		private int index;
		private boolean completed;
		private String startTime;
		private String endTime;
		private MMTools tools = ToolsBB.getInstance();

		/**
		 * Initialises all the command message parameters
		 */
		public CommandMessage() {
			clearData();
		}

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
		 * Adds command message information to command message.
		 * 
		 * @param _index
		 *            index of command message
		 */
		public void setMessage(int _index) {
			this.index = _index;
		}

		/**
		 * Initialises all the command message parameters.
		 */
		public void clearData() {
			index = 0;
			completed = false;
			startTime = "0";
			endTime = "0";
		}

		/**
		 * Returns the type of the command message.
		 * 
		 * @return type of the command message
		 */
		public int getType() {
			return type;
		}

		/**
		 * Retrieves the time when command processing starts.
		 */
		public String getTime() {
			return startTime;
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
		public String getREST() {
			return Registration.getRegID() + Tools.ServerQueryStringSeparator
					+ "0" + type + Tools.ServerQueryStringSeparator + index
					+ Tools.ServerQueryStringSeparator + (completed ? 1 : 0)
					+ Tools.ServerQueryStringSeparator + startTime
					+ Tools.ServerQueryStringSeparator + endTime;
		}

		/**
		 * Sets the time when the command started to be processed.
		 */
		public void setStartTime() {
			startTime = tools.getDate();
		}

		/**
		 * Retrieves the time when the command started to be processed.
		 * 
		 * @return Time when the command started to be processed.
		 */
		public String getStartTime() {
			return startTime;
		}

		/**
		 * Sets the current time when the command finished processing.
		 */
		public void setEndTime() {
			endTime = tools.getDate();
		}

		/**
		 * Gets the current time when the command finished processing.
		 * 
		 * @return Time when the command finished processing
		 */
		public String getEndTime() {
			return endTime;
		}
	}
}
