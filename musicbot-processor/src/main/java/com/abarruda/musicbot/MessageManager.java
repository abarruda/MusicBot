package com.abarruda.musicbot;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.abarruda.musicbot.config.Configuration;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class MessageManager extends TelegramLongPollingBot {
		
	private final EventBus eventBus;
	private final String userName;
	private final String token;
	
	@Inject
	public MessageManager(
			final Configuration config,
			final EventBus eventBus) {
		this.eventBus = eventBus;
		this.userName = config.getConfig(Configuration.BOT_NAME);
		this.token = config.getConfig(Configuration.API_TOKEN);
	}

	@Override
	public void onUpdateReceived(final Update update) {
		
		if (update.hasMessage()) {
			eventBus.post(update.getMessage());
		}
		
		if (update.hasCallbackQuery()) {
			eventBus.post(update.getCallbackQuery());
		}
			
	}

	@Override
	public String getBotUsername() {
		return userName;
	}

	@Override
	public String getBotToken() {
		return token;
	}

}
