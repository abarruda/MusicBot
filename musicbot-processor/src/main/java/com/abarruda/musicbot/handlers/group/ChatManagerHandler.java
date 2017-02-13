package com.abarruda.musicbot.handlers.group;

import java.util.concurrent.Callable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.handlers.MessageHandler;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ChatManagerHandler implements MessageHandler {
	
	private static final Logger logger = LogManager.getLogger(ChatManagerHandler.class);
	
	private final ChatManager chatManager;
	
	@Inject
	public ChatManagerHandler(final ChatManager chatManager) {
		this.chatManager = chatManager;
	}

	@Subscribe
	public void handleMessage(final Update update) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (update.hasMessage()) {
					final Message message = update.getMessage();
					final User user = message.getFrom();
					try {
						chatManager.update(
								message.getChatId().toString(),
								message.getChat().getTitle(), 
								user.getId(),
								user.getFirstName(),
								user.getLastName(),
								message.getDate().toString());
					} catch (Exception e) {
						logger.error("Error updating ChatManager!", e);
					}
				}
				
			}
		}).start();
	}
	
	@Override
	public Callable<BotResponse> handleMessage(Message message) {
		return new Callable<BotResponse>() {

			@Override
			public BotResponse call() throws Exception {
				final User user = message.getFrom();
				try {
					chatManager.update(
							message.getChatId().toString(),
							message.getChat().getTitle(), 
							user.getId(),
							user.getFirstName(),
							user.getLastName(),
							message.getDate().toString());
				} catch (Exception e) {
					logger.error("Error updating ChatManager!", e);
				}
				return null;
			}
			
		};
		
	}

}
