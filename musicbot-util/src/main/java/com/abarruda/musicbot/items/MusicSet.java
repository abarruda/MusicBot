package com.abarruda.musicbot.items;

import java.util.List;

import org.bson.Document;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MusicSet extends RemoteContent {
	
	public static enum Status {
		ACTIVE, INACTIVE;
	}
	
	public static class Metadata {
		public final static String FIELD_TITLE = "title";
		public final static String FIELD_IMAGE_URL = "imageUrl";
		
		public String title;
		public String imageUrl;
		
		public static Metadata getMetadataFromDocument(final Document doc) {
			if (doc != null) {
				final Metadata metadata = new Metadata();
				metadata.title = doc.getString(FIELD_TITLE);
				metadata.imageUrl = doc.getString(FIELD_IMAGE_URL);
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
		
		public static Document toDoc(final Metadata metadata) {
			final Document doc = new Document();
			doc.append(FIELD_TITLE, metadata.title);
			doc.append(FIELD_IMAGE_URL, metadata.imageUrl);
			return doc;
		}
	}
	
	public final static String FIELD_METADATA = "metadata";
	public final static String FIELD_STATUS = "status";
	
	public static class Play {
		public final static String FIELD_USER_ID = "userId";
		public final static String FIELD_DATE_OF_PLAY = "dateOfPlay";
		
		public int userId;
		public int dateOfPlay;
		
		public static Play getPlayFromDocument(final Document doc) {
			if (doc != null) {
				final Play play = new Play();
				final int userId = doc.getInteger(FIELD_USER_ID);
				final int date = doc.getInteger(FIELD_DATE_OF_PLAY);
				play.userId = userId;
				play.dateOfPlay = date;
				return play;
			}
			return null;
		}
		
		public Document toDoc() {
			return toDoc(this);
		}
		
		public static Document toDoc(final Play play) {
			final Document doc = new Document();
			doc.append(FIELD_USER_ID, play.userId);
			doc.append(FIELD_DATE_OF_PLAY, play.dateOfPlay);
			return doc;
		}
		
	}
	
	public final static String FIELD_PLAYS = "plays";
	
	public String status;
	public Metadata metadata;
	public List<Play> plays;
	
	public MusicSet(final RemoteContent remoteContent, final String status, final Metadata metadata, final List<Play> plays) {
		super(remoteContent._id, remoteContent.url, remoteContent.type, remoteContent.originalUser,
				remoteContent.originalDate, remoteContent.references);
		this.status = status;
		this.metadata = metadata;
		this.plays = plays;
	}
	
	@SuppressWarnings("unchecked")
	public static MusicSet getSetFromDoc(Document doc) {
		final String status = doc.getString(FIELD_STATUS);
		final Metadata metadata = Metadata.getMetadataFromDocument(doc.get(FIELD_METADATA, Document.class));
		final List<Play> plays = Lists.newArrayList();
		
		final Object playsFromDoc = doc.get(FIELD_PLAYS);
		if (playsFromDoc != null) {
			for (Document play : (List<Document>)playsFromDoc) {
				plays.add(Play.getPlayFromDocument(play));
			}
		}
		return new MusicSet(getRemoteContentFromDoc(doc), status, metadata, plays);
	}
	
	public Document toDoc() {
		return toDoc(this);
	}
	
	public static Document toDoc(final MusicSet set) {
		final Document musicSetDoc = RemoteContent.toDoc(
				new RemoteContent(set._id, set.url, set.type, set.originalUser, set.originalDate, set.references ));
		musicSetDoc.append(FIELD_STATUS, Strings.nullToEmpty(set.status));
		musicSetDoc.append(FIELD_METADATA, set.metadata.toDoc());
		
		final List<Document> plays = Lists.newArrayList();
		for (final Play play : set.plays) {
			plays.add(play.toDoc());
		}
		musicSetDoc.append(FIELD_PLAYS, plays);
		
		return musicSetDoc;
	}

}
