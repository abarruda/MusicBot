package com.abarruda.musicbot.processor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.abarruda.musicbot.handlers.CallbackQueryHandler;
import com.abarruda.musicbot.handlers.MessageHandler;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.abarruda.musicbot.processor.responder.responses.ButtonResponse.Button;

import jersey.repackaged.com.google.common.collect.Lists;

public class LongPollingProcessor extends TelegramLongPollingBot implements Respondable {
	
	private static final Logger logger = LogManager.getLogger(LongPollingProcessor.class);
	
	private final String botName;
	private final String botToken;
	private final ConcurrentLinkedQueue<BotResponse> responseQueue;
	private final List<MessageHandler> groupHandlers;
	private final List<MessageHandler> privateHandlers;
	private final List<CallbackQueryHandler> callbackQueryHandlers;
	private final ExecutorService handlerExecutor;
	private final ExecutorCompletionService<BotResponse> ecs;
	
	public LongPollingProcessor(final String botName, final String botToken, 
			final ConcurrentLinkedQueue<BotResponse> responseQueue) {
		this.botName = botName;
		this.botToken = botToken;
		this.responseQueue = responseQueue;
		this.groupHandlers = Lists.newArrayList();
		this.privateHandlers = Lists.newArrayList();
		this.callbackQueryHandlers = Lists.newArrayList();
		this.handlerExecutor = Executors.newFixedThreadPool(10);
		this.ecs = new ExecutorCompletionService<>(handlerExecutor);
		
	}
	
	public void addGroupHandler(MessageHandler handler) {
		this.groupHandlers.add(handler);
	}
	
	public void addPrivateHandler(MessageHandler handler) {
		this.privateHandlers.add(handler);
	}
	
	public void addCallbackQueryHandler(CallbackQueryHandler handler) {
		this.callbackQueryHandlers.add(handler);
	}

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		logger.info("Received update...");
		
		final List<Callable<BotResponse>> handlerCallables = Lists.newArrayList();
		
		if (update.hasMessage()) {
			final Message message = update.getMessage();
			
			if (message.isGroupMessage()) {
				logger.info(update);
				
				for (final MessageHandler handler : groupHandlers) {
					handlerCallables.add(handler.handleMessage(message));
				}
				
			} else if(message.isUserMessage()) {
				System.out.println("PRIVATE message: " + message);
				for (final MessageHandler handler : privateHandlers) {
					handlerCallables.add(handler.handleMessage(message));
				}
				
			} else {
				System.out.println("OTHER message: " + message);
			}
			
		} else if (update.hasCallbackQuery()) {
			logger.info("CallbackQuery: " + update);
			final CallbackQuery query = update.getCallbackQuery();
			for (final CallbackQueryHandler handler : callbackQueryHandlers) {
				handlerCallables.add(handler.handleCallbackQuery(query));
			}
		}
		
		int futureCount = 0;
		for (final Callable<BotResponse> handlerCallable : handlerCallables) {
			ecs.submit(handlerCallable);
			futureCount++;
		}
		
		for (int i=0; i < futureCount; i++) {
			try {
				final Future<BotResponse> future = ecs.take();
				final BotResponse response = future.get();
				if (response != null) {
					this.responseQueue.add(response);
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e);
			}
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
		sendMessage(sendMessageRequest);
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
		sendMessage(message);
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
				button.setCallbackData(buttonMapping.buttonData);
				inlineButtonRow.add(button);
			}
			buttonRows.add(inlineButtonRow);
		}
		
		keyboard.setKeyboard(buttonRows);
		
		message.setReplayMarkup(keyboard);
		sendMessage(message);
	}
	
}
