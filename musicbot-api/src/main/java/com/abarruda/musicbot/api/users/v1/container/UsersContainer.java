package com.abarruda.musicbot.api.users.v1.container;

import java.util.Set;

import com.abarruda.musicbot.api.users.v1.resource.UsersResource;
import com.abarruda.musicbot.items.User;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;

public class UsersContainer extends UsersResource {

	@Override
	public Set<User> getUsersByChatId(String chatId) {
		final DatabaseFacade db = MongoDbFacade.getMongoDb();
		return db.getUsersForChat(chatId);
	}

}
