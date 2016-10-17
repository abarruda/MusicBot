package com.abarruda.musicbot.handlers;

import java.util.concurrent.Callable;

import org.telegram.telegrambots.api.objects.CallbackQuery;

import com.abarruda.musicbot.processor.responder.responses.BotResponse;

public interface CallbackQueryHandler {
	
	public Callable<BotResponse> handleCallbackQuery(final CallbackQuery query);

}
