package com.abarruda.musicbot.handlers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.api.objects.Message;

import jersey.repackaged.com.google.common.collect.Lists;

public class CommandUtil {
	
	public static class Command {
		
		private String command;
		private List<String> args;
		
		private Command(String command, List<String> args) {
			this.command = command;
			this.args = args;
		}
		
		public String getCommand() {
			return this.command;
		}
		
		public List<String> getArguments() {
			return this.args;
		}
		
	}
	
	// Will separate command and arguments, arguments enclosed in quotes
	private static final Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
	
	public static Command getCommandFromMessage(Message message) {
		final List<String> argList = Lists.newArrayList();
		String command = "";
		boolean foundCommand = false;
		final Matcher regexMatcher = regex.matcher(message.getText());
		
		while (regexMatcher.find()) {
			if (!foundCommand) {
				command = regexMatcher.group();
				foundCommand = true;
			} else {
				argList.add(regexMatcher.group());
			}		    
		}
		
		return new Command(command, argList);
	}

}
