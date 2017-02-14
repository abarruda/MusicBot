package com.abarruda.musicbot.api.users.v1.container;

import java.util.Set;

import com.abarruda.musicbot.api.users.v1.resource.UsersResource;
import com.abarruda.musicbot.items.User;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.google.inject.Inject;

public class UsersContainer extends UsersResource {
	
	private final DatabaseFacade db;
	
	@Inject
	public UsersContainer(final DatabaseFacade db) {
		this.db = db;
	}

	@Override
	public Set<User> getUsersByChatId(String chatId) {
		return db.getUsersForChat(chatId);
	}

}
