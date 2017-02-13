package com.abarruda.musicbot.processor.responder;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import com.abarruda.musicbot.MessageManager;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.abarruda.musicbot.processor.responder.responses.ButtonResponse.Button;
import com.google.common.collect.Queues;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

import jersey.repackaged.com.google.common.collect.Lists;

public class Responder implements Runnable{
	
	private static final Logger logger = LogManager.getLogger(Responder.class);
	
	private final MessageManager messageManager;
	
	private final ConcurrentLinkedQueue<BotResponse> queueOfWork;
	private final ScheduledExecutorService executor;
	
	@Inject
	public Responder(final MessageManager messageManager) {
		this.messageManager = messageManager;
		
		this.queueOfWork = Queues.newConcurrentLinkedQueue();
		
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("responder-pool-thread-%d").build();
		this.executor = Executors.newScheduledThreadPool(5, threadFactory);
	}
	
	public void start() {
		this.executor.scheduleAtFixedRate(this, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	@Subscribe
	public void respondWithMessage(final BotResponse response) {
		this.queueOfWork.add(response);
	}
	
	@Override
	public void run() {
		try {
			BotResponse response = queueOfWork.poll();
			
			if (response != null) {
				if (response instanceof TextResponse) {
					respondWithTextMessage((TextResponse)response);
				} else if (response instanceof TextButtonResponse) {
					respondWithButtonMessage((TextButtonResponse)response);
				} else if (response instanceof InlineButtonResponse) {
					respondWithInlineButtonMessage((InlineButtonResponse)response);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot send response!", e);
		}
		
	}
	
	public void respondWithTextMessage(TextResponse response) throws TelegramApiException {
		final SendMessage sendMessageRequest = new SendMessage();
		sendMessageRequest.setReplayMarkup(null);
        sendMessageRequest.setChatId(response.chatId);
        sendMessageRequest.setText(response.text);
        sendMessageRequest.enableMarkdown(response.markdownEnabled);
        if (response.silent) {
        	sendMessageRequest.disableWebPagePreview();
        	sendMessageRequest.disableNotification();
        }
        //TODO: handle response to check and reinsert into the queue if necessary
		messageManager.sendMessage(sendMessageRequest);
	}
	
	public void respondWithButtonMessage(TextButtonResponse response) throws TelegramApiException {
		final SendMessage message = new SendMessage();
		message.setChatId(response.chatId);
		message.setText(response.text);
		message.enableMarkdown(true);
		if (response.silent) {
        	message.disableWebPagePreview();
        	message.disableNotification();
        }
		
		final ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
		final List<KeyboardRow> buttonRows = Lists.newArrayList();
		for (List<Button> row: response.buttonTextMapping) {
			final KeyboardRow buttonRow = new KeyboardRow();
			for (Button button: row) {
				final KeyboardButton newButton = new KeyboardButton();
				newButton.setText(button.buttonText);
				buttonRow.add(newButton);
			}
			buttonRows.add(buttonRow);
		}
		keyboard.setKeyboard(buttonRows);
		keyboard.setOneTimeKeyboad(true);
		keyboard.setResizeKeyboard(true);
		
		message.setReplayMarkup(keyboard);
		messageManager.sendMessage(message);
	}
	
	public void respondWithInlineButtonMessage(InlineButtonResponse response) throws TelegramApiException {
		final SendMessage message = new SendMessage();
		message.setChatId(response.chatId);
		message.setText(response.text);
		message.enableMarkdown(true);
		if (response.silent) {
        	message.disableWebPagePreview();
        	message.disableNotification();
        }
		
		final InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		final List<List<InlineKeyboardButton>> buttonRows = Lists.newArrayList();
		
		for (final List<Button> buttonRowMapping : response.buttonTextMapping) {
			final List<InlineKeyboardButton> inlineButtonRow = Lists.newArrayList();
			for (final Button buttonMapping : buttonRowMapping) {
				final InlineKeyboardButton button = new InlineKeyboardButton();
				button.setText(buttonMapping.buttonText);
				if (buttonMapping.buttonUrl != null) {
					button.setUrl(buttonMapping.buttonUrl);
				} else {
					button.setCallbackData(buttonMapping.buttonData);
				}
				inlineButtonRow.add(button);
			}
			buttonRows.add(inlineButtonRow);
		}
		
		keyboard.setKeyboard(buttonRows);
		
		message.setReplayMarkup(keyboard);
		messageManager.sendMessage(message);
	}

}
