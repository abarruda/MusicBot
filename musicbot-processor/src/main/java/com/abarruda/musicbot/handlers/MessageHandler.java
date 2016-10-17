package com.abarruda.musicbot.handlers;

import java.util.concurrent.Callable;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.processor.responder.responses.BotResponse;

public interface MessageHandler  {
	
	public Callable<BotResponse> handleMessage(final Message message);

}
