package com.abarruda.musicbot.handlers.direct;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.handlers.CallbackQueryHandler;
import com.abarruda.musicbot.handlers.MessageHandler;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.ForceReplyTextResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse.InlineButtonResponseBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class TermResponseInputHandler implements MessageHandler, CallbackQueryHandler {
	
	private static final Logger logger = LogManager.getLogger(TermResponseInputHandler.class);
	
	private final static String COMMAND = "Auto Responder";
	private final static String CREATOR = "aaron";
	private final static long USER_INTERACTION_TIMEOUT = 3L;
	
	private DatabaseFacade db;
	private ChatManager chatManger;
	private Cache<String, AutoResponderInputState> cache;
	
	private static class AutoResponderInputState {
		
		private String chatId;
		private String term;
		private String response;
		
		public boolean isComplete() {
			return (term != null && response != null);
		}
		
	}
	
	public TermResponseInputHandler() {
		this.db = MongoDbFacade.getMongoDb();
		this.chatManger = ChatManager.getChatManager();
		
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.expireAfterAccess(USER_INTERACTION_TIMEOUT, TimeUnit.MINUTES)
				.build();
	}
	
	private static String slanderProofMatchingTerm(String term, String user) {
		final StringBuilder modifiedTerm = new StringBuilder();
		if (term.toLowerCase().contains(CREATOR)) {
			int position = 0;
			while (position < term.length()) {
				int index = term.toLowerCase().substring(position).indexOf(CREATOR);
				if (index > -1) {
					modifiedTerm.append(term.substring(position, index));
					modifiedTerm.append(user);
					position = index + CREATOR.length();
				} else {
					modifiedTerm.append(term.substring(position));
					break;
				}
			}
			return modifiedTerm.toString();
		}
		return term;
	}
	
	public static void main(String[] args) {
		System.out.println(slanderProofMatchingTerm("Aaron is so cool", "Matt"));
		System.out.println(slanderProofMatchingTerm("Aaron", "you mean cool dude"));
		System.out.println(slanderProofMatchingTerm("Is Aaron around?", "Nick"));
	}
	
	private TextResponse getInactiveTextResponse(String chatId) {
		return TextResponse.createResponse(
				chatId, 
				"You must be active in a chat within the last week to create an Auto Responder", 
				false, 
				false);
	}
	
	@Override
	public Callable<BotResponse> handleCallbackQuery(final CallbackQuery query) {
		return new Callable<BotResponse>() {

			@Override
			public BotResponse call() throws Exception {
				final String userId = query.getFrom().getId().toString();
				final String directChatId = query.getMessage().getChatId().toString();
				final String chatIdFromButton = query.getData();
				
				if (!chatManger.getChatsForUserFromCache(userId).values().contains(chatIdFromButton)) {
					return getInactiveTextResponse(directChatId);
				}
				
				final String chatIdForAutoResponder = query.getData();
				
				// check if they already have made their allotment
				for (final TermResponse tr : db.getTermResponses(chatIdForAutoResponder)) {
					if (tr.userId.equals(userId)) {
						return TextResponse.createResponse(
								directChatId, 
								"Sorry, you have already made a submission, now fuck off and wait a week!", 
								false,
								false);
					}
				}
				
				final AutoResponderInputState state = new AutoResponderInputState();
				state.chatId = directChatId;
				cache.put(userId, state);
				
				return ForceReplyTextResponse.createResponse(
						directChatId,
						"What term do you want to look for?",
						false, 
						false);
			}
			
		};
	}

	@Override
	public Callable<BotResponse> handleMessage(Message message) {
		return new Callable<BotResponse>() {

			@Override
			public BotResponse call() throws Exception {
				
				if (message.hasText()) {
					final String userId = message.getFrom().getId().toString();
					final String chatId = message.getChatId().toString();
					
					if (message.getText().equals(COMMAND)) {
						
						final Map<String, String> chatsForUser = chatManger.getChatsForUserFromCache(userId);
						if (chatsForUser.isEmpty()) {
							return getInactiveTextResponse(chatId);
						} else {

							final InlineButtonResponseBuilder builder = new InlineButtonResponseBuilder()
									.setChatId(chatId)
									.setSilent(false)
									.setText("Which chat would you like to apply the Auto Responder to?");
							
							for (Map.Entry<String, String> chat : chatsForUser.entrySet()) {
								builder.addButtonRow(InlineButtonResponseBuilder.newButton(chat.getKey(), chat.getValue()));
							}
							return InlineButtonResponse.createResponse(builder);
						}
						
					} else if (cache.getIfPresent(userId) != null) {
						
						final AutoResponderInputState state = cache.getIfPresent(userId);
						
						if (!state.isComplete() && state.term == null) {
							
							final String termFromUser = message.getText();
							state.term = termFromUser;
							cache.put(userId, state);
							
							final StringBuilder responseText = new StringBuilder();
							responseText.append("What response do you want to send when '");
							responseText.append(termFromUser);
							responseText.append("' is seen?");
							
							return ForceReplyTextResponse.createResponse(
									chatId, 
									responseText.toString(),
									false, 
									false);
							
						} else if (!state.isComplete() && state.term != null && state.response == null) {
							String responseFromUser = message.getText();
							responseFromUser = slanderProofMatchingTerm(responseFromUser, message.getFrom().getFirstName());
							
							final TermResponse autoResponse = new TermResponse(userId,
									new Date(),
									state.term, responseFromUser);
							
							db.insertTermResponse(state.chatId, autoResponse);
							cache.invalidate(userId);
							return TextResponse.createResponse(
									chatId, 
									"Successfully created!  Wait a minute for the response to become active.", 
									false,
									false);
							
						} else {
							logger.error("Unexpected state for AutoResponder input!");
						}
						
					} else {
						// Can't do the following as it'll also impact other commands
						//return TextResponse.createSessionExpiredResponse(chatId);
					}
				}
				return null;
			}
			
		};
	}

}
