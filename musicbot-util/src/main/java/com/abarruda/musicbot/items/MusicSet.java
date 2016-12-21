package com.abarruda.musicbot.items;

import org.bson.Document;

import com.google.common.base.Strings;

public class MusicSet extends RemoteContent {
	
	public static enum Status {
		ACTIVE, INACTIVE;
	}
	
	public static class Metadata {
		public String title;
		public String imageUrl;
		
		public static Metadata getMetadataFromDocument(final Document doc) {
			if (doc != null) {
				final Metadata metadata = new Metadata();
				metadata.title = doc.getString("title");
				metadata.imageUrl = doc.getString("imageUrl");
				return metadata;
			}
			return null;
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
	public final static String FIELD_STATUS = "status";
	
	public String status;
	public Metadata metadata;
	
	public MusicSet(final RemoteContent remoteContent, final String status, final Metadata metadata) {
		super(remoteContent._id, remoteContent.url, remoteContent.type, remoteContent.originalUser,
				remoteContent.originalDate, remoteContent.references);
		this.status = status;
		this.metadata = metadata;
	}
	
	public boolean isMusicSet() {
		return this.metadata != null;
	}
	
	public static MusicSet getSetFromDoc(Document doc) {
		final String status = doc.getString("status");
		final Metadata metadata = Metadata.getMetadataFromDocument(doc.get("metadata", Document.class));
		return new MusicSet(getRemoteContentFromDoc(doc), status, metadata);
	}
	
	public Document toDoc() {
		return toDoc(this);
	}
	
	public static Document toDoc(final MusicSet set) {
		final Document musicSetDoc = RemoteContent.toDoc(
				new RemoteContent(set._id, set.url, set.type, set.originalUser, set.originalDate, set.references ));
		musicSetDoc.append(FIELD_STATUS, Strings.nullToEmpty(set.status));
		musicSetDoc.append(FIELD_METADATA, set.metadata.toDoc());
		return musicSetDoc;
	}

}
