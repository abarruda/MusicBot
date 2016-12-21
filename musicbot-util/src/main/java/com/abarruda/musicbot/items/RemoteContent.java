package com.abarruda.musicbot.items;

import java.util.List;

import org.bson.Document;

import com.google.common.collect.Lists;

public class RemoteContent {
	
	public final static String FIELD_URL = "url";
	public final static String FIELD_TYPE = "type";
	public final static String FIELD_ORIGINAL_USER = "originalUser";
	public final static String FIELD_ORIGINAL_DATE = "originalDate";
	public final static String FIELD_REFERENCES = "references";
	
	public String _id;
	public String url;
	public String type;
	public User originalUser;
	public int originalDate;
	public List<Reference> references;
	
	public RemoteContent(final String id, final String url, final String type, 
			final User originalUser, final int originalDate, final List<Reference> references) {
		this._id = id;
		this.url = url;
		this.type = type;
		this.originalUser = originalUser;
		this.originalDate = originalDate;
		this.references = references;
	}
	
	@SuppressWarnings("unchecked")
	public static RemoteContent getRemoteContentFromDoc(Document doc) {
		final List<Reference> references = Lists.newArrayList();
		
		for(Document reference : (List<Document>)doc.get("references")) {
			references.add(Reference.getReferenceFromDoc(reference));
		}
		
		return new RemoteContent(
				doc.getObjectId("_id").toString(),
				doc.getString("url"),
				doc.getString("type"),
				User.getUserFromDocument(doc.get("originalUser", Document.class)),
				doc.getInteger("originalDate"),
				references
				);
	}
	
	public Document toDoc() {
		return toDoc(this);
	}
	
	public static Document toDoc(final RemoteContent remoteContent) {
		final Document remoteContentDoc = new Document("_id", remoteContent._id);
		remoteContentDoc.append(FIELD_URL, remoteContent.url);
		remoteContentDoc.append(FIELD_TYPE, remoteContent.type);
		
		remoteContentDoc.append(FIELD_ORIGINAL_USER, remoteContent.originalUser.toDoc());
		remoteContentDoc.append(FIELD_ORIGINAL_DATE, remoteContent.originalDate);
		
		final List<Document> references = Lists.newArrayList();
		for (final Reference ref : remoteContent.references) {
			references.add(ref.toDoc());
		}
		remoteContentDoc.append(FIELD_REFERENCES, references);
		
		return remoteContentDoc;
		
	}
	
	@Override
	public String toString() {
		return this.url;
	}

}
