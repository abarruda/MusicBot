package com.abarruda.musicbot.handlers.direct;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.processor.responder.responses.ForceReplyTextResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class FeedbackHandler {
	
	private static final Logger logger = LogManager.getLogger(FeedbackHandler.class);
	
	public static final String FEEDBACK_COMMAND = "Send Feedback/Report Bug";
	
	private static final String OUTPUT_FILE = "feedback_bugs.log";
	private final static long USER_INTERACTION_TIMEOUT = 7L;
	
	private final EventBus eventBus;
	private Cache<String, Boolean> cache;
	
	@Inject
	public FeedbackHandler(final EventBus eventBus) {
		this.eventBus = eventBus;
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.expireAfterAccess(USER_INTERACTION_TIMEOUT, TimeUnit.DAYS)
				.build();
	}
	
	@Subscribe
	public void handleMessage(TelegramMessage.PrivateMessage privateMessage) {
		final Message message = privateMessage.getMessage();
		if (message.hasText()) {
			
			final String chatId = message.getChatId().toString();
			final String userId = message.getFrom().getId().toString();
			
			if (message.getText().equals(FEEDBACK_COMMAND) && cache.getIfPresent(userId) == null) {
				cache.put(userId, true);
				eventBus.post(ForceReplyTextResponse.createResponse(
						chatId, 
						"What feedback would you like to give?", 
						true,
						false));
			} else if (cache.getIfPresent(userId) != null && cache.getIfPresent(userId)) {
				final String userFirstName = message.getFrom().getFirstName();
				final String userLastName = message.getFrom().getLastName();
				
				final StringBuilder feedback = new StringBuilder();
				feedback.append(new Date());
				feedback.append(" - ");
				feedback.append(userFirstName);
				feedback.append(" ");
				feedback.append(userLastName);
				feedback.append("(");
				feedback.append(userId);
				feedback.append(") - '");
				feedback.append(message.getText());
				feedback.append("'");
				feedback.append("\n");
				
				FileWriter file = null;
				try {
					file = new FileWriter(OUTPUT_FILE);
					file.write(feedback.toString());
				} catch (IOException e) {
					logger.error("Cannot write to feedback log!", e);
				} finally {
					try {
						file.close();
					} catch (IOException e) {
						logger.error("Cannot close file!", e);
					}
					cache.invalidate(userId);
					eventBus.post(TextResponse.createResponse(chatId, "Thank you for your feedback!", true, false));
				}
			}
		
		}

	}
		

}
