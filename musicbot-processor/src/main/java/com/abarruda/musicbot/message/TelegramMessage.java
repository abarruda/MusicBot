package com.abarruda.musicbot.message;

import org.telegram.telegrambots.api.objects.Message;

public class TelegramMessage {

	private final Message message;
	
	private TelegramMessage(final Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	public static class GroupMessage extends TelegramMessage {
		public GroupMessage(final Message message) {
			super(message);
		}
	}
	
	public static class PrivateMessage extends TelegramMessage {
		public PrivateMessage(final Message message) {
			super(message);
		}
	}
	

}
