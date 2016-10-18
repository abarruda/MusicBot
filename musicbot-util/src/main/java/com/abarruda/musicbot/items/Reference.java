package com.abarruda.musicbot.items;

import org.bson.Document;

public class Reference {
	
	public User user;
	public int date;
	
	public static Reference getReferenceFromDoc(Document doc) {
		final Reference reference = new Reference();
		final User user = User.getUserFromDocument(doc.get("user", Document.class));
		reference.user = user;
		reference.date = doc.getInteger("date");
		return reference;
	}

}
