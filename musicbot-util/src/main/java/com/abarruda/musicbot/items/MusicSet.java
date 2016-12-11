package com.abarruda.musicbot.items;

import java.util.List;

import org.bson.Document;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MusicSet {
	
	public static enum Status {
		ACTIVE, INACTIVE;
	}
	
	public static class Metadata {
		public String title;
		public String imageUrl;
		
		public static Metadata getMetadataFromDocument(final Document doc) {
			final Metadata metadata = new Metadata();
			
			if (doc != null) {
				metadata.title = doc.getString("title");
				metadata.imageUrl = doc.getString("imageUrl");
			}
			return metadata;
		}
		
		public boolean isEmpty() {
			return Strings.isNullOrEmpty(imageUrl);
		}
		
		public Document toDoc() {
			return toDoc(this);
		}
		
		public Document toDoc(final Metadata metadata) {
			final Document doc = new Document();
			doc.append("title", metadata.title);
			doc.append("imageUrl", metadata.imageUrl);
			return doc;
		}
	}
	
	public final static String FIELD_METADATA = "metadata";
	public final static String FIELD_ORIGINAL_DATE = "originalDate";
	public final static String FIELD_ORIGINAL_USER = "originalUser";
	public final static String FIELD_REFERENCES = "references";
	public final static String FIELD_STATUS = "status";
	public final static String FIELD_TYPE = "type";
	public final static String FIELD_URL = "url";
	
	public String _id;
	public String url;
	public String type;
	public String status;
	public User originalUser;
	public int originalDate;
	public Metadata metadata;
	public List<Reference> references;
	
	public static MusicSet getSetFromDoc(Document doc) {
		final MusicSet set = new MusicSet();
		set._id = doc.getObjectId("_id").toString();
		set.url = doc.getString("url");
		set.type = doc.getString("type");
		set.status = doc.getString("status");
		set.originalUser = User.getUserFromDocument(doc.get("originalUser", Document.class));
		set.originalDate = doc.getInteger("originalDate");
		set.metadata = Metadata.getMetadataFromDocument(doc.get("metadata", Document.class));
		set.references = Lists.newArrayList();
		@SuppressWarnings("unchecked")
		final List<Document> references = (List<Document>)doc.get("references");
		for (final Document reference : references) {
			set.references.add(Reference.getReferenceFromDoc(reference));
		}
		return set;
	}
	
	public Document toDoc() {
		return toDoc(this);
	}
	
	public static Document toDoc(final MusicSet set) {
		final Document musicSetDoc = new Document("_id", set._id);
		musicSetDoc.append(FIELD_URL, set.url);
		musicSetDoc.append(FIELD_TYPE, set.type);
		musicSetDoc.append(FIELD_STATUS, Strings.nullToEmpty(set.status));
		musicSetDoc.append(FIELD_ORIGINAL_USER, set.originalUser.toDoc());
		musicSetDoc.append(FIELD_ORIGINAL_DATE, set.originalDate);
		musicSetDoc.append(FIELD_METADATA, set.metadata.toDoc());
		
		final List<Document> references = Lists.newArrayList();
		for (final Reference ref : set.references) {
			references.add(ref.toDoc());
		}
		musicSetDoc.append(FIELD_REFERENCES, references);
		
		return musicSetDoc;
	}

}
