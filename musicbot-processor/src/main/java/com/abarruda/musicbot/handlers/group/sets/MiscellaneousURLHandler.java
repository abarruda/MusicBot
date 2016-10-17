package com.abarruda.musicbot.handlers.group.sets;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;

import com.abarruda.musicbot.items.DetectedSet;
import com.abarruda.musicbot.items.SetType;

public class MiscellaneousURLHandler extends AbstractSetHandler {

	protected MiscellaneousURLHandler(Message message, MessageEntity entity) {
		super(message, entity);
	}

	@Override
	public DetectedSet getSet() {
		return new DetectedSet(SetType.MISC, 
				getUrl(this.message.getText(), entity), 
				this.message.getDate(), 
				this.message.getFrom());
	}

}
