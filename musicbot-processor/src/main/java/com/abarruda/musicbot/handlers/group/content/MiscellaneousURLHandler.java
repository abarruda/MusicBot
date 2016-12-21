package com.abarruda.musicbot.handlers.group.content;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.ContentType;

public class MiscellaneousURLHandler extends AbstractRemoteContentHandler {

	protected MiscellaneousURLHandler(Message message, MessageEntity entity) {
		super(message, entity);
	}

	@Override
	public DetectedContent getContent() {
		return new DetectedContent(ContentType.MISC, 
				getUrl(this.message.getText(), entity), 
				this.message.getDate(), 
				getFromUser(this.message));
	}

}
