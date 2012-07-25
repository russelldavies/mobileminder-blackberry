package com.mmtechco.mobileminder.command;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.mmtechco.mobileminder.data.ActivityLog;
import com.mmtechco.mobileminder.net.ErrorMessage;
import com.mmtechco.mobileminder.net.Message;
import com.mmtechco.mobileminder.net.Reply;
import com.mmtechco.mobileminder.net.Reply.ParseException;
import com.mmtechco.mobileminder.net.Response;
import com.mmtechco.mobileminder.net.Server;
import com.mmtechco.util.Logger;
import com.mmtechco.util.MMTools;
import com.mmtechco.util.ToolsBB;

/**
 * Responsible for contacting the server and getting responses. It is also
 * responsible for processing responses.
 */
public class Commander {
	private static final int time = 1000 * 60 * 5; // 5 mins
	private static final int noCommandIndex = 0;

	private static Vector components;

	private static MMTools tools = ToolsBB.getInstance();
	private static Logger logger = Logger.getLogger(Commander.class);

	public static void addComponent(Controllable component) {
		components.addElement(component);
	}
	
	public static void startProcessing() {
		new Timer().schedule(new CommandTask(), time);
	}

	/**
	 * Contacts to server and gets reply from server. Process reply if valid.
	 */
	private static class CommandTask extends TimerTask {
		public void run() {
			while (true) {
				try {
					Response response = Server.get(new CommandMessage().toString());
					Reply.Command reply = new Reply.Command(response.getContent());
					// No more commands to process
					if (reply.index == noCommandIndex) {
						logger.debug("No Commands to Process");
						return;
					}

					// There are commands so find appropriate target and process
					// command
					for (Enumeration e = components.elements(); e.hasMoreElements();) {
						Controllable target = (Controllable) e.nextElement();
						
						if (!target.isTarget(reply.getTarget())) {
							break;
						}

						CommandMessage message = new CommandMessage( reply.index);
						if (target.processCommand(reply.getArgs())) {
							// Command executed successfully
							message.succeeded(true);
							logger.debug("Executed command " + reply.index
											+ ": " + reply.getArgs());
						} else {
							// Command failed
							logger.debug("Failed to execute command "
									+ reply.index + ": " + reply.getArgs());
							message.succeeded(false);
						}
						Server.get(message.toString());
					}
				} catch (IOException e) {
					logger.warn("Connection problem: " + e.getMessage());
					return;
				} catch (ParseException e) {
					ActivityLog.addMessage(new ErrorMessage(e));
					return;
				}
			}
		}
	}

	private static class CommandMessage extends Message {
		private String startTime;

		/**
		 * Message format:
		 * <ul>
		 * <li>Command index
		 * <li>Completed (0 == false, 1 == true)
		 * <li>Time command started executing
		 * <li>Time command stopped executing
		 * </ul>
		 */
		public CommandMessage(int index) {
			super(Message.COMMAND);
			add(String.valueOf(index));
			startTime = tools.getDate();
		}
		
		public CommandMessage() {
			this(0);
			succeeded(false);
		}

		public void succeeded(boolean completed) {
			add(completed ? "true" : "false");
			add(startTime);
			add(tools.getDate());
		}
	}
}
