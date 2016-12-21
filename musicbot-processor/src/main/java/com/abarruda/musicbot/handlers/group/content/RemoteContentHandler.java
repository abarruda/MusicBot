package com.abarruda.musicbot.handlers.group.content;

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
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.collect.Lists;

import jersey.repackaged.com.google.common.collect.Sets;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

public class RemoteContentHandler implements MessageHandler {
	
	private static final Logger logger = LogManager.getLogger(RemoteContentHandler.class);
	
	private static final String TYPE_URL = "url";
	
	private DatabaseFacade db;
	
	public RemoteContentHandler() {
		db = MongoDbFacade.getMongoDb();
	}
		
	private static Set<DetectedContent> getContent(final Message message, final User user) {
		final Set<DetectedContent> sets = Sets.newHashSet();
		if (message.getEntities() != null) {
			for (final MessageEntity entity : message.getEntities()) {
				if (entity.getType().equals(TYPE_URL)) {
					try {
						final AbstractRemoteContentHandler contentHandler = AbstractRemoteContentHandler.getHandler(message, entity);
						final DetectedContent contentInMessage = contentHandler.getContent();
						sets.add(contentInMessage);
						
						logger.info("Detected '" + contentInMessage.type.name() + "' by '" + contentInMessage.user.firstName + "': " + contentInMessage.url);
					} catch (MalformedURLException e) {
						logger.error("Could not handle detected content!", e);
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
				
				final Set<DetectedContent> remoteContents = getContent(message, message.getFrom());
				
				if (remoteContents.size() > 0) {
					final List<DetectedContent> remoteContentToBeInserted = Lists.newArrayList();
					final List<String> messagesToSend = Lists.newArrayList();
					
					for(final DetectedContent set : remoteContents) {
						try {
							final RemoteContent trackedRemoteContent = db.getRemoteContent(chatId, set.url);
							
							if (trackedRemoteContent == null) {
								// insert
								remoteContentToBeInserted.add(set);
							} else {						
								// Update the DB
								db.updateRemoteContentReference(chatId, set);
								
								
								// Send the message to the outgoing queue
								final int referenceCount = trackedRemoteContent.references.size() + 1 + 1; // +1 for the original post, +1 for this post
								final StringBuilder messageText = new StringBuilder();
								messageText.append(trackedRemoteContent.url);
								messageText.append(" was originally posted by ");
								messageText.append(trackedRemoteContent.originalUser.firstName);
								messageText.append(" on ");
								messageText.append(new Date(trackedRemoteContent.originalDate * 1000L));
								messageText.append(".  Referenced ");
								messageText.append(referenceCount);
								messageText.append(" time(s).");
								messagesToSend.add(messageText.toString());								
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
					
					if (remoteContentToBeInserted.size() > 0) {
						db.insertRemoteContent(chatId, remoteContentToBeInserted);
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