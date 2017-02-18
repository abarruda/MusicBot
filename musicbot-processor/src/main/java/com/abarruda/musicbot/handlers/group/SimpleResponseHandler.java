package com.abarruda.musicbot.handlers.group;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.config.Configuration;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

public class SimpleResponseHandler {
	
	private static final Logger logger = LogManager.getLogger(SimpleResponseHandler.class);
	
	private final static String RESPONSES_FILE = "responses.txt";
	
	private final Configuration configuration;
	private final EventBus eventBus;
	private static Map<String, String> mapping;
	private final DatabaseFacade db;
	private Map<String, Map<String, String>> chatToTermResponseMapping = Maps.newConcurrentMap();
	private ScheduledExecutorService executor;
	
	private final Runnable termResponseMaintainer = new Runnable() {
		
		@Override
		public void run() {
			
			for (final String chatId : db.getChatIds().keySet()) {
				final Map<String, String> updatedMapping = Maps.newConcurrentMap();
				
				for(final TermResponse tr : db.getTermResponses(chatId)) {
					final ZonedDateTime dateOfTermExpiry = ZonedDateTime.now().plusDays(
							0-Integer.parseInt(configuration.getConfig(Configuration.AUTORESPONSE_DURATION)));
					if (tr.date.toInstant().isAfter(dateOfTermExpiry.toInstant())) {
						updatedMapping.put(tr.term, tr.response);
					} else {
						db.deleteTermResponse(chatId, tr);
					}
				}
				updatedMapping.putAll(mapping);
				chatToTermResponseMapping.put(chatId, updatedMapping);
				
			}
			
		}
	};
	
	@Inject
	public SimpleResponseHandler(final Configuration configuration, final EventBus eventBus, final DatabaseFacade db) {
		this.configuration = configuration;
		this.eventBus = eventBus;
		mapping = loadResponsesFromFile();
		this.db = db;
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Term-Response-Maintainer-thread-%d").build();
		executor = Executors.newScheduledThreadPool(1, threadFactory);
		executor.scheduleAtFixedRate(termResponseMaintainer, 0, 15, TimeUnit.SECONDS);
	}
	
	private Map<String, String> loadResponsesFromFile() {
		final Map<String, String> loadedResponses = Maps.newHashMap();
		
		try {
			final FileInputStream input = new FileInputStream(RESPONSES_FILE);
			final BufferedReader br = new BufferedReader(new InputStreamReader(input));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				final Iterable<String> result = Splitter.on("==").trimResults().split(line);
				final String trigger = Iterables.getFirst(result, null);
				final String response = Iterables.getLast(result);
				loadedResponses.put(trigger, response);
			}
			
			input.close();
		} catch (FileNotFoundException e) {
			logger.warn("No responses file found.");
		} catch (IOException e) {
			logger.error("Error reading responses file.");
		}
		
		return loadedResponses;
	}
	
	static class HandleMessageRunnable implements Runnable {
		
		final private Message message;
		final private EventBus eventBus;
		final private Map<String, Map<String, String>> mapping;
		
		public HandleMessageRunnable(final Message message, final EventBus eventBus, Map<String, Map<String, String>> mapping) {
			this.message = message;
			this.eventBus = eventBus;
			this.mapping = mapping;
		}

		@Override
		public void run() {
			if (message.hasText()) {
				final String chatId = message.getChatId().toString();
				final Map<String, String> mappingForChat = mapping.get(chatId);
				
				if (mappingForChat != null) {
					for (final String term : mappingForChat.keySet()) {
						if (message.getText().toLowerCase().contains(term.toLowerCase())) {
							eventBus.post(TextResponse.createResponse(
									message.getChatId().toString(), 
									mappingForChat.get(term), 
									true,
									false));
						}
					}
				}
				
			}
			
		}
		
	}
	
	@Subscribe
	public void handleMessage(final TelegramMessage.GroupMessage groupMessage) {
		final Message message = groupMessage.getMessage();
		new Thread(new HandleMessageRunnable(message, eventBus, chatToTermResponseMapping)).start();

	}

}
