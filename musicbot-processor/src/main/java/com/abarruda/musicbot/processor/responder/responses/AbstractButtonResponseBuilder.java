package com.abarruda.musicbot.processor.responder.responses;

import java.util.List;

import com.abarruda.musicbot.handlers.CallbackQueryUtil;
import com.abarruda.musicbot.processor.responder.responses.ButtonResponse.Button;
import com.google.common.collect.Lists;

public abstract class AbstractButtonResponseBuilder<B extends AbstractButtonResponseBuilder<?>> {
	
	final Class<B> builderType;
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
	
	public static Button newButtonWithData(final String buttonText, final String buttonSource, final String buttonData) {
		return newButton(buttonText, buttonSource + CallbackQueryUtil.DELIMITER + buttonData, null);
	}
	
	public static Button newButtonWithUrl(final String buttonText, final String url) {
		return newButton(buttonText, null, url);
	}
	
	public static Button newButton(final String buttonText) {
		return newButton(buttonText, null, null);
	}
	
	/**
	 * It is important to note that either buttonData OR buttonUrl will be used.  If both are used, 
	 * buttonUrl takes precedence.  URL capability only used in {@code InlineButtonResponse}s
	 * @param buttonText
	 * @param buttonData
	 * @param buttonUrl
	 * @return
	 */
	private static Button newButton(String buttonText, String buttonData, String buttonUrl) {
		final Button newButton = new Button();
		newButton.buttonText = buttonText;
		newButton.buttonData = buttonData;
		newButton.buttonUrl = buttonUrl;
		return newButton;
	}

}
