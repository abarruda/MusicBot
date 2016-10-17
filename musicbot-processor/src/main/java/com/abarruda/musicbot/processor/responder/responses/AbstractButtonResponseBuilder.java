package com.abarruda.musicbot.processor.responder.responses;

import java.util.List;

import com.abarruda.musicbot.processor.responder.responses.ButtonResponse.Button;
import com.google.common.collect.Lists;

public abstract class AbstractButtonResponseBuilder<B extends AbstractButtonResponseBuilder<?>> {
	
	private final Class<B> builderType;
	String chatId;
	String text;
	boolean silent;
	List<List<Button>> buttonTextMapping;
	
	public <I> AbstractButtonResponseBuilder(final Class<B> builderType) {
		this.builderType = builderType;
	}
	
	public B setChatId(String chatId) {
		this.chatId = chatId;
		return builderType.cast(this);
	}
	
	public B setText(String text) {
		this.text = text;
		return builderType.cast(this);
	}
	
	public B setSilent(boolean silent) {
		this.silent = silent;
		return builderType.cast(this);
	}
	
	public B addButtonRow(Button... buttons) {
		if (buttonTextMapping == null) {
			buttonTextMapping = Lists.newLinkedList();
		}
		final List<Button> buttonRow = Lists.newLinkedList();
		for (Button buttonText : buttons) {
			buttonRow.add(buttonText);
		}
		buttonTextMapping.add(buttonRow);
		
		return builderType.cast(this);
	}
	
	public abstract ButtonResponse build();
	
	
	public static Button newButton(String buttonText, String buttonData) {
		final Button newButton = new Button();
		newButton.buttonText = buttonText;
		newButton.buttonData = buttonData;
		return newButton;
	}

}
