package com.abarruda.musicbot.api.chats.v1.container;

import com.abarruda.musicbot.api.chats.v1.resource.ChatsResource;
import com.abarruda.musicbot.items.Chat;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.google.inject.Inject;

public class ChatsContainer extends ChatsResource {
	
	final DatabaseFacade db;
	
	@Inject
	public ChatsContainer(final DatabaseFacade db) {
		this.db = db;
	}

	@Override
	public Chat getChat(String id) {
		return db.getChatByTelegramId(id);
	}

}
