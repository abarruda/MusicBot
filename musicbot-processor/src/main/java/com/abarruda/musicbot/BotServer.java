package com.abarruda.musicbot;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.logging.BotLogger;

import com.abarruda.musicbot.config.Config;
import com.abarruda.musicbot.handlers.direct.FeedbackHandler;
import com.abarruda.musicbot.handlers.direct.HelpMessageHandler;
import com.abarruda.musicbot.handlers.direct.StatsHandler;
import com.abarruda.musicbot.handlers.direct.TermResponseInputHandler;
import com.abarruda.musicbot.handlers.group.ChatManagerHandler;
import com.abarruda.musicbot.handlers.group.LoggingHandler;
import com.abarruda.musicbot.handlers.group.SimpleResponseHandler;
import com.abarruda.musicbot.handlers.group.sets.SetHandler;
import com.abarruda.musicbot.processor.LongPollingProcessor;
import com.abarruda.musicbot.processor.metadata.MusicSetMetadataProcessor;
import com.abarruda.musicbot.processor.responder.Responder;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.google.common.collect.Queues;

public class BotServer {
	private static final Logger logger = LogManager.getLogger(BotServer.class);
	
	public static void main(String[] args) {
		logger.info("Bot starting.");
		BotLogger.setLevel(java.util.logging.Level.FINEST);
		Config.initializeConfigs(args[0]);
		
		final ConcurrentLinkedQueue<BotResponse> responseQueue = Queues.newConcurrentLinkedQueue();
		final LongPollingProcessor processor = new LongPollingProcessor(Config.getConfig(Config.BOT_NAME), 
				Config.getConfig(Config.API_TOKEN), responseQueue);
		
		// preload ChatManager caches
		ChatManager.getChatManager();
		
		new MusicSetMetadataProcessor().start();
		
		final TermResponseInputHandler autoResponder = new TermResponseInputHandler();
		
		processor.addGroupHandler(new ChatManagerHandler());
		processor.addGroupHandler(new SimpleResponseHandler());
		processor.addGroupHandler(new SetHandler());
		processor.addGroupHandler(new LoggingHandler());
		
		processor.addPrivateHandler(new HelpMessageHandler());
		processor.addPrivateHandler(new FeedbackHandler());
		processor.addPrivateHandler(new StatsHandler());
		processor.addPrivateHandler(autoResponder);
		
		processor.addCallbackQueryHandler(autoResponder);
		
		final Responder responder = Responder.initializeResponder(responseQueue, processor);
		responder.start();

		final TelegramBotsApi telegramBotApi = new TelegramBotsApi();
		try {
			telegramBotApi.registerBot(processor);
		} catch (Exception e) {
			logger.fatal("Telegram connection error!", e);
			System.exit(1);
		}

	}

}
