package com.mmtechco.mobileminder.control;

import com.mmtechco.mobileminder.Controller;
import com.mmtechco.mobileminder.Registration;
import com.mmtechco.mobileminder.data.LocalDataAccess;
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
 * Responsible for contacting server and getting response from server. It is
 * also responsible for processing response.
 */
public class Commander extends Thread {
	private static final String TAG = "Commander";
	
	private Server myServer;
	private Controllable componentList[];
	private final int time;
	private LocalDataWriter actLog;
	private final int commandSignal = 0;
	private Logger logger = Logger.getInstance();

	/**
	 * Used by Controller to initialises the {@link LocalDataAccess} object and
	 * {@link Controllable} objects.
	 */
	public Commander(LocalDataWriter actlog,
			Controller controller, Controllable[] components) {
		myServer = new Server(actlog);
		// myServer.start();
		time = 1000 * 60 * 5;
		componentList = components;
		this.actLog = actlog;
	}

	/**
	 * Contacts to server and gets reply from server. If reply is process-able
	 * then process it.
	 */
	public void run() {
		while (true) {
			boolean commandQueued = true;
			while (commandQueued) {
				CommandMessage commandMessageFirst = new CommandMessage();
				commandMessageFirst.setMessage(commandSignal);
				Reply myReply = myServer.contactServer(commandMessageFirst);
				logger.log(TAG, "COM Index:" + myReply.getIndex());

				// 0 if tasks
				if (commandSignal == myReply.getIndex() || myReply.isError()) {
					commandQueued = false;
					logger.log(TAG, "No Commands to Process");
					break;
				} else {
					CommandMessage commandMessage = new CommandMessage();

					for (int count = 0; count < componentList.length; count++) {
						Controllable aTarget = componentList[count];

						if (aTarget.isTarget(myReply.getTarget())) {
							commandMessage.setStartTime();
							if (aTarget.processCommand(myReply.getArgs())) { // ran
																				// fine
																				// :)

								logger.log(TAG, "Sending Command Reply=ran fine} index:"
										+ myReply.getIndex());

								commandMessage.setEndTime();
								commandMessage.setMessage(myReply.getIndex(),
										true);

								logger.log(TAG, "Sending Command Reply Message:index="
										+ myReply.getIndex()
										+ " REST:"
										+ commandMessage.getREST());

								logger.log(TAG, "Commander Sending... "
										+ commandMessage.getREST());

								myServer.contactServer(commandMessage);
							} else { // No joy :(

								logger.log(TAG, "Sending Command Reply=no joy} index:"
										+ myReply.getIndex());

								commandMessage.setEndTime();
								commandMessage.setMessage(myReply.getIndex(),
										false);
								myServer.contactServer(commandMessage);
							}

							logger.log(TAG, "Out of command reply= clearing data} index:"
									+ myReply.getIndex());
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
			// TODO Auto-generated method stub
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
			return
					Registration.getRegID() +
					Tools.ServerQueryStringSeparator +
					"0" + type +
					Tools.ServerQueryStringSeparator +
					index +
					Tools.ServerQueryStringSeparator +
					(completed ? 1 : 0) +
					Tools.ServerQueryStringSeparator +
					startTime +
					Tools.ServerQueryStringSeparator +
					endTime;
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
