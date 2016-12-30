package com.abarruda.musicbot.handlers;

import java.util.Map;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse.InlineButtonResponseBuilder;

public class ChatListUtil {
	
	private static TextResponse getInactiveTextResponse(String chatId) {
		return TextResponse.createResponse(
				chatId, 
				"You must be active in a chat within the last week to create an Auto Responder", 
				false, 
				false);
	}
	
	public static BotResponse getChatListForUser(final String responseChatId, final int userId, final String description, final String source) {
		final ChatManager chatManager = ChatManager.getChatManager();
		
		final Map<String, String> chatsForUser = chatManager.getChatsForUserFromCache(userId);
		if (chatsForUser.isEmpty()) {
			return getInactiveTextResponse(responseChatId);
		} else {

			final InlineButtonResponseBuilder builder = new InlineButtonResponseBuilder()
					.setChatId(responseChatId)
					.setSilent(false)
					.setText(description);
			
			for (Map.Entry<String, String> chat : chatsForUser.entrySet()) {
				builder.addButtonRow(InlineButtonResponseBuilder.newButtonWithData(chat.getKey(), source, chat.getValue()));
			}
			return InlineButtonResponse.createResponse(builder);
		
		}
	}

}
