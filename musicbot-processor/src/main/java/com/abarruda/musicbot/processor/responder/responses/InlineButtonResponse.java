package com.abarruda.musicbot.processor.responder.responses;

import java.util.List;

public class InlineButtonResponse extends ButtonResponse {
	
	private InlineButtonResponse(String chatId, String text, boolean silent, 
			List<List<Button>> buttonTextMapping) {
		super(chatId, text, silent, buttonTextMapping);
	}
	
	public static class InlineButtonResponseBuilder extends AbstractButtonResponseBuilder<InlineButtonResponseBuilder> {
		
		public InlineButtonResponseBuilder() {
			super(InlineButtonResponseBuilder.class);
		}
		
		@Override
		public InlineButtonResponse build() {
			return new InlineButtonResponse(chatId, text, silent, buttonTextMapping);
		}
		
	}
	
	public static InlineButtonResponse createResponse(InlineButtonResponseBuilder builder) {
		return builder.build();
	}

}
