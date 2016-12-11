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
	
	public Document toDoc() {
		return toDoc(this);
	}
	
	public static Document toDoc(final Reference ref) {
		final Document doc = new Document();
		doc.append("user", ref.user.toDoc());
		doc.append("date", ref.date);
		return doc;
	}

}
