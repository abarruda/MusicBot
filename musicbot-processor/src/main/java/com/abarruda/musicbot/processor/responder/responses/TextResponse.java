package com.abarruda.musicbot.processor.responder.responses;

public class TextResponse extends BotResponse {
	
	public String chatId;
	public String text;
	public boolean silent;
	public boolean markdownEnabled;
	
	TextResponse(String chatId, String text, boolean silent, boolean markdownEnabled) {
		this.chatId = chatId;
		this.text = text;
		this.silent = silent;
		this.markdownEnabled = markdownEnabled;
	}
	
	public static TextResponse createResponse(String chatId, String text, boolean silent, boolean enableMarkdown) {
		return new TextResponse(chatId, text, silent, enableMarkdown);
	}
	
	public static TextResponse createSessionExpiredResponse(String chatId) {
		return TextResponse.createResponse(
				chatId,
				"Your session expired, please select an option from the main menu.",
				true,
				true);
	}
}
