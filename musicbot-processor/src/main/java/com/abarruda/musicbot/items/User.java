package com.abarruda.musicbot.items;

import org.bson.Document;

public class User {
	public int userId;
	public String firstName;
	public String lastName;
	
	public static User getUserFromDocument(Document doc) {
		final User user = new User();
		user.userId = doc.getInteger("userId");
		user.firstName = doc.getString("firstName");
		user.lastName = doc.getString("lastName");
		return user;
	}

}
