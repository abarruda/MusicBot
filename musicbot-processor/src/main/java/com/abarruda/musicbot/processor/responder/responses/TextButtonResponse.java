package com.abarruda.musicbot.processor.responder.responses;

import java.util.List;

public class TextButtonResponse extends ButtonResponse {
	
	private TextButtonResponse(String chatId, String text, boolean silent, 
			List<List<Button>> buttonTextMapping) {
		super(chatId, text, silent, buttonTextMapping);
	}
	
	public static class TextButtonResponseBuilder extends AbstractButtonResponseBuilder<TextButtonResponseBuilder> {
		
		public TextButtonResponseBuilder() {
			super(TextButtonResponseBuilder.class);
		}

		@Override
		public TextButtonResponse build() {
			return new TextButtonResponse(chatId, text, silent, buttonTextMapping);
		}
		
	}
	
	public static TextButtonResponse createResponse(TextButtonResponseBuilder builder) {
		return builder.build();
	}

}
