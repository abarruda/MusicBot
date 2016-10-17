package com.abarruda.musicbot.processor.responder.responses;

public class ForceReplyTextResponse extends TextResponse {
	
	private ForceReplyTextResponse(String chatId, String text, boolean silent, boolean markdownEnabled) {
		super(chatId, text, silent, markdownEnabled);
	}
	
	public static ForceReplyTextResponse createResponse(
			final String chatId, final String text, final boolean silent, boolean enableMarkdown) {
		return new ForceReplyTextResponse(chatId, text, silent, enableMarkdown);
	}

}
