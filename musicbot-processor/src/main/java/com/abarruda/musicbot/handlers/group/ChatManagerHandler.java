package com.abarruda.musicbot.handlers.group;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.message.TelegramMessage;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ChatManagerHandler {
	
	private static final Logger logger = LogManager.getLogger(ChatManagerHandler.class);
	
	private final ChatManager chatManager;
	
	@Inject
	public ChatManagerHandler(final ChatManager chatManager) {
		this.chatManager = chatManager;
	}

	@Subscribe
	public void handleMessage(final TelegramMessage.GroupMessage groupMessage) {
		final Message message = groupMessage.getMessage();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
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
		}).start();
	}

}
