package com.abarruda.musicbot.modules;

import com.abarruda.musicbot.BotServer;
import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.MessageManager;
import com.abarruda.musicbot.config.ConfigurationModule;
import com.abarruda.musicbot.handlers.direct.BrowseSetsHandler;
import com.abarruda.musicbot.handlers.direct.FeedbackHandler;
import com.abarruda.musicbot.handlers.direct.HelpMessageHandler;
import com.abarruda.musicbot.handlers.direct.StatsHandler;
import com.abarruda.musicbot.handlers.direct.TermResponseInputHandler;
import com.abarruda.musicbot.handlers.group.ChatManagerHandler;
import com.abarruda.musicbot.handlers.group.LoggingHandler;
import com.abarruda.musicbot.handlers.group.SimpleResponseHandler;
import com.abarruda.musicbot.handlers.group.content.RemoteContentHandler;
import com.abarruda.musicbot.persistence.DatabaseModule;
import com.abarruda.musicbot.processor.metadata.MusicSetMetadataProcessor;
import com.abarruda.musicbot.processor.responder.Responder;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BotModule extends AbstractModule {
	
	private final String configFileLocation;
	
	public BotModule(final String configFileLocation) {
		this.configFileLocation = configFileLocation;
	}

	@Override
	protected void configure() {
		install(new ConfigurationModule(configFileLocation));
		install(new DatabaseModule());
		
		bind(BotServer.class).in(Scopes.SINGLETON);
		bind(EventBus.class).in(Scopes.SINGLETON);
		bind(MessageManager.class).in(Scopes.SINGLETON);
		bind(Responder.class).in(Scopes.SINGLETON);
		bind(ChatManager.class).in(Scopes.SINGLETON);
		bind(MusicSetMetadataProcessor.class).in(Scopes.SINGLETON);
		
		// Handlers
		bind(ChatManagerHandler.class);
		bind(SimpleResponseHandler.class);
		bind(RemoteContentHandler.class);
		bind(LoggingHandler.class);
		bind(HelpMessageHandler.class);
		bind(FeedbackHandler.class);
		bind(StatsHandler.class);
		bind(TermResponseInputHandler.class);
		bind(BrowseSetsHandler.class);
	}

}
