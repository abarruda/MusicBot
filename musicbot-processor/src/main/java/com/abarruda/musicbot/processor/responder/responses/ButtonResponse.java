package com.abarruda.musicbot.processor.responder.responses;

import java.util.List;

public class ButtonResponse extends BotResponse {
	
	public static class Button {
		public String buttonText;
		public String buttonData;
		public String buttonUrl;
	}
	
	public String chatId;
	public String text;
	public boolean silent;
	public List<List<Button>> buttonTextMapping;
	
	public ButtonResponse(String chatId, String text, boolean silent, 
			List<List<Button>> buttonTextMapping) {
		this.chatId = chatId;
		this.text = text;
		this.silent = silent;
		this.buttonTextMapping = buttonTextMapping;
	}
}
