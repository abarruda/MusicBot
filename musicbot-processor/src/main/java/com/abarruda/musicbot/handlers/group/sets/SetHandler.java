package com.abarruda.musicbot.handlers.group.sets;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.User;

import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.abarruda.musicbot.handlers.MessageHandler;
import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.collect.Lists;

import jersey.repackaged.com.google.common.collect.Sets;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

public class SetHandler implements MessageHandler {
	
	private static final Logger logger = LogManager.getLogger(SetHandler.class);
	
	private static final String TYPE_URL = "url";
	
	private DatabaseFacade db;
	
	public SetHandler() {
		db = MongoDbFacade.getMongoDb();
	}
		
	private static Set<DetectedContent> getSets(final Message message, final User user) {
		final Set<DetectedContent> sets = Sets.newHashSet();
		if (message.getEntities() != null) {
			for (final MessageEntity entity : message.getEntities()) {
				if (entity.getType().equals(TYPE_URL)) {
					try {
						final AbstractSetHandler setHandler = AbstractSetHandler.getHandler(message, entity);
						final DetectedContent setInMessage = setHandler.getSet();
						sets.add(setInMessage);
						
						logger.info("Detected '" + setInMessage.type.name() + "' by '" + setInMessage.user.firstName + "': " + setInMessage.url);
					} catch (MalformedURLException e) {
						logger.error("Could not handle detected set!", e);
					}
				}
			}
		}
		return sets;
	}
	
	public Callable<BotResponse> handleMessage(final Message message) {
		final String chatId = message.getChatId().toString();
		
		return new Callable<BotResponse>() {

			@Override
			public BotResponse call() throws Exception {
				
				final Set<DetectedContent> sets = getSets(message, message.getFrom());
				
				if (sets.size() > 0) {
					final List<DetectedContent> setsToBeInserted = Lists.newArrayList();
					final List<String> messagesToSend = Lists.newArrayList();
					
					for(final DetectedContent set : sets) {
						try {
							final MusicSet trackedSet = db.getSet(chatId, set.url);
							
							if (trackedSet == null) {
								// insert
								setsToBeInserted.add(set);
							} else {						
								// Update the DB
								db.updateSetReference(chatId, set);
								
								
								// Send the message to the outgoing queue
								final int referenceCount = trackedSet.references.size() + 1 + 1; // +1 for the original post, +1 for this post
								final StringBuilder messageText = new StringBuilder();
								messageText.append(trackedSet.url);
								messageText.append(" was originally posted by ");
								messageText.append(trackedSet.originalUser.firstName);
								messageText.append(" on ");
								messageText.append(new Date(trackedSet.originalDate * 1000L));
								messageText.append(".  Referenced ");
								messageText.append(referenceCount);
								messageText.append(" time(s).");
								messagesToSend.add(messageText.toString());								
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
					
					if (setsToBeInserted.size() > 0) {
						db.insertSets(chatId, setsToBeInserted);
					}
					
					if (messagesToSend.size() > 0) {
						final StringBuilder responseMessageText = new StringBuilder();
						String delim = "";
						for (final String responseMessage : messagesToSend) {
							responseMessageText.append(delim);
							responseMessageText.append(responseMessage);
							delim = "\n";
						}
						return TextResponse.createResponse(message.getChatId().toString(), responseMessageText.toString(), true, false);
					}
				}
				
				return null;
			}
			
		};
	}


}
