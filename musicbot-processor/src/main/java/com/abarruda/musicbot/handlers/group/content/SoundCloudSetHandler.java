package com.abarruda.musicbot.handlers.group.content;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.ContentType;

public class SoundCloudSetHandler extends AbstractRemoteContentHandler {
	
	private static final Logger logger = LogManager.getLogger(SoundCloudSetHandler.class);

	protected SoundCloudSetHandler(Message message, MessageEntity entity) {
		super(message, entity);
		logger.info("Detected Soundcloud set!");
	}
	
	protected static String getUrl(final String messageText, final MessageEntity entity) {
		String url = messageText.substring(entity.getOffset(), entity.getOffset() + entity.getLength());
		final int indexOfQueryComponent = url.indexOf("?");
		if (indexOfQueryComponent > -1) {
			url = url.substring(0, indexOfQueryComponent);
		}
		return url;
	}

	@Override
	public DetectedContent getContent() {
		return new DetectedContent(ContentType.SOUNDCLOUD, 
				getUrl(this.message.getText(), entity), 
				this.message.getDate(),
				getFromUser(this.message));
	}

}
