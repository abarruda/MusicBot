package com.abarruda.musicbot.handlers.group.content;

import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.User;

import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import jersey.repackaged.com.google.common.collect.Sets;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

public class RemoteContentHandler {
	
	private static final Logger logger = LogManager.getLogger(RemoteContentHandler.class);
	
	private static final String TYPE_URL = "url";
	
	private final EventBus eventBus;
	private final DatabaseFacade db;
	
	@Inject
	public RemoteContentHandler(final EventBus eventBus, final DatabaseFacade db) {
		this.db = db;
		this.eventBus = eventBus;
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
	
	@Subscribe
	public void handleMessage(final TelegramMessage.GroupMessage groupMessage) {
		final Message message = groupMessage.getMessage();
		if (message.hasText()) {
			
			final String chatId = message.getChatId().toString();
			
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
					eventBus.post(TextResponse.createResponse(message.getChatId().toString(), responseMessageText.toString(), true, false));
				}
			}
		}
		
	}
}
