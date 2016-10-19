package com.abarruda.musicbot.items;

import org.bson.Document;

public class User {
	public int userId;
	public String firstName;
	public String lastName;
	
	public User(final int id, final String firstName, final String lastName) {
		this.userId = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public static User getUserFromDocument(Document doc) {
		return new User(doc.getInteger("userId"), doc.getString("firstName"), doc.getString("lastName"));
	}

}
