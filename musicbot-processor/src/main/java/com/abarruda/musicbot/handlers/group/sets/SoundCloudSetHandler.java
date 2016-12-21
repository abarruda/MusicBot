package com.abarruda.musicbot.handlers.group.sets;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.SetType;

public class SoundCloudSetHandler extends AbstractSetHandler {
	
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
	public DetectedContent getSet() {
		return new DetectedContent(SetType.SOUNDCLOUD, 
				getUrl(this.message.getText(), entity), 
				this.message.getDate(),
				getFromUser(this.message));
	}

}
