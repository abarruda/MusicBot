package com.abarruda.musicbot.handlers.group.content;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.ContentType;

public class YouTubeSetHandler extends AbstractRemoteContentHandler {
	
	private static final Logger logger = LogManager.getLogger(YouTubeSetHandler.class);

	protected YouTubeSetHandler(Message message, MessageEntity entity) {
		super(message, entity);
		logger.info("Detected YouTube set!");
	}

	@Override
	public DetectedContent getContent() {
		return new DetectedContent(ContentType.YOUTUBE, 
				getUrl(this.message.getText(), entity), 
				this.message.getDate(),
				getFromUser(this.message));
	}

}
