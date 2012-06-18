package com.mmtechco.mobileminder.net;

import com.mmtechco.mobileminder.prototypes.COMMAND_TARGETS;
import com.mmtechco.mobileminder.prototypes.MMTools;
import com.mmtechco.util.ToolsBB;

public abstract class Reply {
	private static final MMTools tools = ToolsBB.getInstance();

	String[] fields;
	public String content;
	// Shared fields
	public String id;
	public int type;

	public Reply(String content) throws ParseException {
		if (content == null || content.length() == 0) {
			throw new ParseException("Content is null or empty");
		}
		try {
			this.content = content;
			fields = tools.split(content, Message.SEPARATOR);

			id = fields[0];
			type = Integer.valueOf(fields[1]).intValue();
		} catch (RuntimeException e) {
			throw new ParseException(e);
		}
	}

	public static class Regular extends Reply {
		public boolean error;
		public String info;

		public Regular(String content) throws ParseException {
			super(content);
			if (type == Message.COMMAND) {
				throw new ParseException(
						"Received a Command message but did not expect one");
			}
			try {
				error = Integer.parseInt(fields[2]) != 0;
				info = fields[3];
			} catch (RuntimeException e) {
				throw new ParseException(e);
			}
		}
	}

	public static class Command extends Reply {
		public int index;
		private String target;
		private String args;

		public Command(String content) throws ParseException {
			super(content);
			if (type != Message.COMMAND) {
				throw new ParseException(
						"Expected Command message but did not receive one");
			}
			try {
				index = Integer.parseInt(fields[2]);
				// Message contains additional fields
				if (index != 0) {
					target = fields[3];
					args = fields[4];
				}
			} catch (RuntimeException e) {
				throw new ParseException(e);
			}
		}

		public COMMAND_TARGETS getTarget() {
			return COMMAND_TARGETS.from(target);
		}

		public String[] getArgs() {
			return tools.split(args, "|");
		}
	}

	public static class ParseException extends Exception {
		public ParseException(String s) {
			super(s);
		}

		public ParseException(Exception e) {
			super(e.getClass().getName() + ":" + e.getMessage());
		}
	}
}
