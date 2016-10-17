package com.abarruda.musicbot.handlers.group.sets;

import java.net.MalformedURLException;
import java.net.URL;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedSet;
import com.abarruda.musicbot.items.SetType;

public abstract class AbstractSetHandler {
	
	protected Message message;
	protected MessageEntity entity; 
	
	protected AbstractSetHandler(final Message message, final MessageEntity entity) {
		this.message = message;
		this.entity = entity;
	}
	
	public abstract DetectedSet getSet();
	
	protected static String getUrl(final String messageText, final MessageEntity entity) {
		return messageText.substring(entity.getOffset(), entity.getOffset() + entity.getLength());
	}
	
	private static SetType determineSetType(final String messageText, final MessageEntity entity) throws MalformedURLException {
		final URL url = new URL(messageText.substring(entity.getOffset(), entity.getOffset() + entity.getLength()));
		for (SetType type : SetType.values()) {
			if (type.isOfType(url)) {
				return type;
			}
		}
		return SetType.MISC;
	}
	
	public static AbstractSetHandler getHandler(final Message message, final MessageEntity entity) throws MalformedURLException {
		switch (determineSetType(message.getText(), entity)) {
		case SOUNDCLOUD: 
			return new SoundCloudSetHandler(message, entity);
		
		case YOUTUBE:
			return new YouTubeSetHandler(message, entity);
			
		default:
			return new MiscellaneousURLHandler(message, entity);
		}
	}

}
