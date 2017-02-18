package com.abarruda.musicbot;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.abarruda.musicbot.config.Configuration;
import com.abarruda.musicbot.message.TelegramMessage;
import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class MessageManager extends TelegramLongPollingBot {
	
	private static final MetricRegistry metrics = new MetricRegistry();
		
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
		metrics.meter("updates-received").mark();
		
		if (update.hasMessage()) {
			final Message message = update.getMessage();
			
			if (message.isGroupMessage()) {
				eventBus.post(new TelegramMessage.GroupMessage(message));
			} else if (message.isUserMessage()) {
				eventBus.post(new TelegramMessage.PrivateMessage(message));
			} else {
				//TODO: throw exception and log
			}
		} else if (update.hasCallbackQuery()) {
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
