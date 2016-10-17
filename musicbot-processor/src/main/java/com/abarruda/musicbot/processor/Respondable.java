package com.abarruda.musicbot.processor;

import org.telegram.telegrambots.TelegramApiException;

import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;

public interface Respondable {
	
	void respondWithTextMessage(TextResponse response) throws TelegramApiException;
	
	void respondWithButtonMessage(TextButtonResponse response) throws TelegramApiException;
	
	void respondWithInlineButtonMessage(InlineButtonResponse response) throws TelegramApiException;
}
