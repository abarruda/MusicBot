package com.abarruda.musicbot;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.logging.BotLogger;

import com.abarruda.musicbot.config.Config;
import com.abarruda.musicbot.handlers.direct.BrowseSetsHandler;
import com.abarruda.musicbot.handlers.direct.FeedbackHandler;
import com.abarruda.musicbot.handlers.direct.HelpMessageHandler;
import com.abarruda.musicbot.handlers.direct.StatsHandler;
import com.abarruda.musicbot.handlers.direct.TermResponseInputHandler;
import com.abarruda.musicbot.handlers.group.ChatManagerHandler;
import com.abarruda.musicbot.handlers.group.LoggingHandler;
import com.abarruda.musicbot.handlers.group.SimpleResponseHandler;
import com.abarruda.musicbot.handlers.group.content.RemoteContentHandler;
import com.abarruda.musicbot.modules.BotModule;
import com.abarruda.musicbot.processor.metadata.MusicSetMetadataProcessor;
import com.abarruda.musicbot.processor.responder.Responder;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class BotServer {
	private static final Logger logger = LogManager.getLogger(BotServer.class);
	
	private final EventBus eventBus;
	private final MessageManager messageManager;
	private final Responder responder;
	private final ChatManager chatManager;
	private final MusicSetMetadataProcessor metadataProcessor;
	
	private final ChatManagerHandler chatManagerHandler;
	private final SimpleResponseHandler simpleResponseHandler;
	private final RemoteContentHandler remoteContentHandler;
	private final LoggingHandler loggingHandler;
	private final HelpMessageHandler helpMessageHandler;
	private final FeedbackHandler feedbackHandler;
	private final StatsHandler statsHandler;
	private final TermResponseInputHandler termResponseInputHandler;
	private final BrowseSetsHandler browseSetsHandler;
	
	@Inject
	public BotServer(
			final EventBus eventBus,
			final MessageManager messageManager,
			final com.abarruda.musicbot.processor.responder.Responder responder,
			final ChatManager chatManager,
			final MusicSetMetadataProcessor metadataProcessor,
			// Handlers
			final ChatManagerHandler chatManagerHandler,
			final SimpleResponseHandler simpleResponseHandler,
			final RemoteContentHandler remoteContentHandler,
			final LoggingHandler loggingHandler,
			final HelpMessageHandler helpMessageHandler,
			final FeedbackHandler feedbackHandler,
			final StatsHandler statsHandler,
			final TermResponseInputHandler termResponseInputHandler,
			final BrowseSetsHandler browseSetsHandler) {
		this.eventBus = eventBus;
		this.messageManager = messageManager;
		this.responder = responder;
		this.chatManager = chatManager;
		this.metadataProcessor = metadataProcessor;
		
		this.chatManagerHandler = chatManagerHandler;
		this.simpleResponseHandler = simpleResponseHandler;
		this.remoteContentHandler = remoteContentHandler;
		this.loggingHandler = loggingHandler;
		this.helpMessageHandler = helpMessageHandler;
		this.feedbackHandler = feedbackHandler;
		this.statsHandler = statsHandler;
		this.termResponseInputHandler = termResponseInputHandler;
		this.browseSetsHandler = browseSetsHandler;
	}
	
	public void startServices() {
		this.chatManager.start();
		this.responder.start();
		this.eventBus.register(this.responder);
		this.metadataProcessor.start();
	}
	
	public void registerHandlers() {
		this.eventBus.register(this.chatManagerHandler);
		this.eventBus.register(this.simpleResponseHandler);
		this.eventBus.register(this.remoteContentHandler);
		this.eventBus.register(this.loggingHandler);
		this.eventBus.register(this.helpMessageHandler);
		this.eventBus.register(this.feedbackHandler);
		this.eventBus.register(this.statsHandler);
		this.eventBus.register(this.termResponseInputHandler);
		this.eventBus.register(this.browseSetsHandler);
	}
	
	public void start() {
		startServices();
		
		registerHandlers();
		
		final TelegramBotsApi telegramBotApi = new TelegramBotsApi();
		try {
			telegramBotApi.registerBot(this.messageManager);
		} catch (Exception e) {
			logger.fatal("Telegram connection error!", e);
			System.exit(1);
		}
		
	}
	
	public static void main(String[] args) {
		logger.info("Bot starting.");
		BotLogger.setLevel(java.util.logging.Level.FINEST);
		Config.initializeConfigs(args[0]);
		
		final Injector injector = Guice.createInjector(new BotModule(args[0]));
		final BotServer server = injector.getInstance(BotServer.class);
		server.start();

	}

}
