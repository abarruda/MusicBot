package com.abarruda.musicbot.handlers.direct;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.handlers.CallbackQueryUtil;
import com.abarruda.musicbot.handlers.ChatListUtil;
import com.abarruda.musicbot.handlers.CallbackQueryUtil.CallbackQueryInfo;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.processor.responder.responses.ForceReplyTextResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class TermResponseInputHandler {
	
	private static final Logger logger = LogManager.getLogger(TermResponseInputHandler.class);
	
	public final static String AUTO_RESPONDER_COMMAND = "Auto Responder";
	
	private final static String CREATOR = "aaron";
	private final static long USER_INTERACTION_TIMEOUT = 3L;
	
	private DatabaseFacade db;
	private final EventBus eventBus;
	private final ChatManager chatManager;
	private Cache<Integer, AutoResponderInputState> cache;
	
	private static class AutoResponderInputState {
		
		private String chatId;
		private String term;
		private String response;
		
		public boolean isComplete() {
			return (term != null && response != null);
		}
		
	}
	
	@Inject
	public TermResponseInputHandler(
			final EventBus eventBus,
			final DatabaseFacade db,
			final ChatManager chatManager) {
		this.db = db;
		this.eventBus = eventBus;
		this.chatManager = chatManager;
		
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
	
	@Subscribe
	public void handleCallbackQuery(final CallbackQuery query) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final CallbackQueryInfo queryInfo = CallbackQueryUtil.getInfo(query);
				if (queryInfo.source.equals(TermResponseInputHandler.class.getSimpleName())) {
					
					final int userId = query.getFrom().getId();
					final String directChatId = query.getMessage().getChatId().toString();
					final String chatIdFromButton = queryInfo.data;
					
					if (!chatManager.getChatsForUserFromCache(userId).values().contains(chatIdFromButton)) {
						eventBus.post(getInactiveTextResponse(directChatId));
					}
					
					final String chatIdForAutoResponder = queryInfo.data;
					
					// check if they already have made their allotment
					for (final TermResponse tr : db.getTermResponses(chatIdForAutoResponder)) {
						if (tr.userId == userId) {
							eventBus.post(TextResponse.createResponse(
									directChatId, 
									"Sorry, you have already made a submission, now fuck off and wait a week!", 
									false,
									false));
						}
					}
					
					final AutoResponderInputState state = new AutoResponderInputState();
					state.chatId = chatIdForAutoResponder;
					cache.put(userId, state);
					
					eventBus.post(ForceReplyTextResponse.createResponse(
							directChatId,
							"What term do you want to look for?",
							false, 
							false));
				}
			}
		}).start();
		
	}

	@Subscribe
	public void handleMessage(TelegramMessage.PrivateMessage privateMessage) {
		final Message message = privateMessage.getMessage();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (message.hasText()) {
					final int userId = message.getFrom().getId();
					final String directMessageChatId = message.getChatId().toString();
					
					if (message.getText().equals(AUTO_RESPONDER_COMMAND)) {
						
						eventBus.post(ChatListUtil.getChatListForUser(chatManager, directMessageChatId, userId, 
								"Which chat would you like to apply the Auto Responder to?",
								TermResponseInputHandler.class.getSimpleName()));
						
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
							
							eventBus.post(ForceReplyTextResponse.createResponse(
									directMessageChatId, 
									responseText.toString(),
									false, 
									false));
							
						} else if (!state.isComplete() && state.term != null && state.response == null) {
							String responseFromUser = message.getText();
							responseFromUser = slanderProofMatchingTerm(responseFromUser, message.getFrom().getFirstName());
							
							final TermResponse autoResponse = new TermResponse(userId,
									new Date(),
									state.term, responseFromUser);
							
							db.insertTermResponse(state.chatId, autoResponse);
							cache.invalidate(userId);
							eventBus.post(TextResponse.createResponse(
									directMessageChatId, 
									"Successfully created!  Wait a minute for the response to become active.", 
									false,
									false));
							
						} else {
							logger.error("Unexpected state for AutoResponder input!");
						}
						
					} else {
						// Can't do the following as it'll also impact other commands
						//return TextResponse.createSessionExpiredResponse(chatId);
					}
				}
			}
		}).start();
		
	}

}
