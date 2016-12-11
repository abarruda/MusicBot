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
		return new User(doc.getInteger("userId"), 
				doc.getString("firstName"), 
				doc.getString("lastName"));
	}
	
	public Document toDoc() {
		return toDoc(this);
	}
	
	public static Document toDoc(final User user) {
		final Document doc = new Document();
		doc.append("userId", user.userId)
			.append("firstName", user.firstName)
			.append("lastName", user.lastName);
		return doc;
	}

}
