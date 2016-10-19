package com.abarruda.musicbot.handlers.group.sets;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedSet;
import com.abarruda.musicbot.items.SetType;

public class YouTubeSetHandler extends AbstractSetHandler {
	
	private static final Logger logger = LogManager.getLogger(YouTubeSetHandler.class);

	protected YouTubeSetHandler(Message message, MessageEntity entity) {
		super(message, entity);
		logger.info("Detected YouTube set!");
	}

	@Override
	public DetectedSet getSet() {
		return new DetectedSet(SetType.YOUTUBE, 
				getUrl(this.message.getText(), entity), 
				this.message.getDate(),
				getFromUser(this.message));
	}

}
