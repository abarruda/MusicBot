package com.abarruda.musicbot.items;

import java.util.List;

import org.bson.Document;

import com.google.common.collect.Lists;

public class MusicSet {
	public String _id;
	public String url;
	public String type;
	public User originalUser;
	public int originalDate;
	public List<Reference> references;
	
	public static MusicSet getSetFromDoc(Document doc) {
		final MusicSet set = new MusicSet();
		set._id = doc.getObjectId("_id").toString();
		set.url = doc.getString("url");
		set.type = doc.getString("type");
		set.originalUser = User.getUserFromDocument(doc.get("originalUser", Document.class));
		set.originalDate = doc.getInteger("originalDate");
		set.references = Lists.newArrayList();
		@SuppressWarnings("unchecked")
		final List<Document> references = (List<Document>)doc.get("references");
		for(Document reference : references) {
			set.references.add(Reference.getReferenceFromDoc(reference));
		}
		return set;
	}

}
