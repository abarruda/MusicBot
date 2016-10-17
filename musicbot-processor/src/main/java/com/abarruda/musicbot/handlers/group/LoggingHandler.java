package com.abarruda.musicbot.handlers.group;

import java.util.concurrent.Callable;

import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.handlers.MessageHandler;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;

public class LoggingHandler implements MessageHandler {

	@Override
	public Callable<BotResponse> handleMessage(Message message) {
		return null;
	}

}
