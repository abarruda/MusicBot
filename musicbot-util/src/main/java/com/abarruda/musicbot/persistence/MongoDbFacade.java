package com.abarruda.musicbot.persistence;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.abarruda.musicbot.config.Config;
import com.abarruda.musicbot.items.ContentType;
import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.MusicSet.Play;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.items.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDbFacade implements DatabaseFacade {
	
	private static final Logger logger = LogManager.getLogger(MongoDbFacade.class);
	private static final String CHAT_COLLECTION = "chats";
	private static final String CHAT_MEMBER_PREFIX = "chat-members-";
	private static final String AUTO_RESPONSES_PREFIX = "auto-responses-";
	private static final String REMOTE_CONTENT_PREFIX = "remote-content-";
	private static final String CHAT_ID_BSON = "chatId";
	private static final String CHAT_NAME_BSON = "chatName";
	private static final String USER_ID_BSON = "userId";
	private static final String USER_FIRST_NAME_BSON = "firstName";
	private static final String USER_LAST_NAME_BSON = "lastName";
	private static final String LAST_SEEN_BSON = "lastSeen";
	private static final String SET_URL_BSON = "url";
	
	private final MongoClient mongoClient;
	private final MongoDatabase db;
	
	private MongoDbFacade(String address, String databaseName) {
		mongoClient = new MongoClient(address);
		db = mongoClient.getDatabase(databaseName);
	}
	
	private static MongoDbFacade singleton;
	
	public static MongoDbFacade getMongoDb() {
		if (singleton == null) {
			singleton = new MongoDbFacade(Config.getConfig(Config.DATABASE_ADDRESS), Config.getConfig(Config.DATABASE_NAME));
		}
		return singleton;
	}
	
	private MongoCollection<Document> getChatMemberCollection(String chatId) {
		return db.getCollection(CHAT_MEMBER_PREFIX + chatId);
	}
	
	private MongoCollection<Document> getRemoteContentCollection(String chatId) {
		return db.getCollection(REMOTE_CONTENT_PREFIX + chatId);
	}
	
	private MongoCollection<Document> getAutoResponseCollection(String chatId) {
		return db.getCollection(AUTO_RESPONSES_PREFIX + chatId);
	}
	
	@Override
	public Map<String, String> getChatIds() {
		final Map<String, String> chatIdsToNames = Maps.newHashMap();
		final MongoCursor<Document> cursor = db.getCollection(CHAT_COLLECTION).find().iterator();
		while (cursor.hasNext()) {
			final Document doc = cursor.next();
			chatIdsToNames.put(
					doc.getString(CHAT_ID_BSON),
					doc.getString(CHAT_NAME_BSON));
		}
		return chatIdsToNames;
	}
	
	@Override
	public void storeChat(final String chatId, final String chatName) {
		final MongoCollection<Document> chatCollection = db.getCollection(CHAT_COLLECTION);
		final Document chatDoc = chatCollection.find(eq(CHAT_ID_BSON, chatId)).first();
		
		if (chatDoc == null) {
			final Document newChatDoc = new Document("_id", new ObjectId());
			newChatDoc.append(CHAT_ID_BSON, chatId);
			newChatDoc.append(CHAT_NAME_BSON, chatName);
			db.getCollection(CHAT_COLLECTION).insertOne(newChatDoc);
		} else if (!chatDoc.getString(CHAT_NAME_BSON).equals(chatName)) {
			// chat name changed, update.
			final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(CHAT_NAME_BSON, chatName));
			final UpdateResult result = chatCollection.updateOne(eq(CHAT_ID_BSON, chatId), update);
			if (result.getMatchedCount() != 1) {
				logger.error("Error updating chat name!");
			}
		}
	}
	
	@Override
	public boolean deleteChatId(final String chatId) {
		final DeleteResult result = db.getCollection(CHAT_COLLECTION).deleteOne(eq(CHAT_ID_BSON, chatId));
		return result.getDeletedCount() == 1;
	}
	
	@Override
	public void updateLastSeen(final String chatId, final Integer userId, final String firstName, final String lastName, String dateString) {
		final BasicDBObject update = new BasicDBObject("$set", 
				new BasicDBObject(LAST_SEEN_BSON, dateString)
					.append(USER_FIRST_NAME_BSON, firstName)
					.append(USER_LAST_NAME_BSON, lastName));
		final UpdateResult result = getChatMemberCollection(chatId).updateOne(
				eq(USER_ID_BSON, userId), 
				update, 
				new UpdateOptions().upsert(true));
		
		if (result.getMatchedCount() != 1 && result.getUpsertedId() == null) {
			logger.error("Error updating last seen.");
		}
	}
	
	@Override
	public Set<User> getUsersForChat(final String chatId) {
		final Set<User> users = Sets.newHashSet();
		try {
			final MongoCursor<Document> cursor = getChatMemberCollection(chatId).find().iterator();
			while(cursor.hasNext()) {
				users.add(User.getUserFromDocument(cursor.next()));
			}
		} catch (final Exception e) {
			logger.error("Can't get users!", e);
		}
		return users;
	}

	@Override
	public String getUserLastSeenFromChat(String chatId, int userId) {
		final MongoCollection<Document> collection = getChatMemberCollection(chatId);
		final Document lastSeen = collection.find(eq(USER_ID_BSON, userId)).first();
		if (lastSeen != null) {
			return lastSeen.getString(LAST_SEEN_BSON);
		} else {
			return null;
		}
	}

	@Override
	public void userExpired(String chatId, int userId) {
		DeleteResult result = getChatMemberCollection(chatId).deleteOne(eq(USER_ID_BSON, userId));
		if (result.getDeletedCount() != 1) {
			logger.warn("Error deleting expired user!");
		}
	}
	
	@Override
	public Set<RemoteContent> getRemoteContent(final String chatId) {
		final Set<RemoteContent> remoteContent = Sets.newHashSet();
		
		final MongoCursor<Document> cursor = getRemoteContentCollection(chatId).find().iterator();
		while(cursor.hasNext()) {
			final Document doc = cursor.next();
			try {
				remoteContent.add(RemoteContent.getRemoteContentFromDoc(doc));
			} catch (final Exception e) {
				logger.error("Could not parse content from doc '" + doc.toJson() + "'", e);
			}
		}
		return remoteContent;
	}

	@Override
	public RemoteContent getRemoteContent(final String chatId, final String url) {
		
		final Document dbDoc = getRemoteContentCollection(chatId).find(eq(SET_URL_BSON, url)).first();
		if (dbDoc == null) {
			return null;
		} else {
			return RemoteContent.getRemoteContentFromDoc(dbDoc);
		}
	}
	
	@Override
	public Map<MusicSet, String> getMusicSets() {
		final Map<MusicSet, String> allSets = Maps.newHashMap();
		
		for(final String chatId : getChatIds().keySet()) {
			final MongoCursor<Document> cursor = getRemoteContentCollection(chatId).find().iterator();
			while (cursor.hasNext()) {
				final Document doc = cursor.next();
				final RemoteContent content = RemoteContent.getRemoteContentFromDoc(doc);
				
				// Only return remote content that has been detected as a Music Set
				if (ContentType.isMusicSet(content.type)) {
					allSets.put(MusicSet.getSetFromDoc(doc), chatId);
				}
			}
		}
		
		return allSets;
	}
	
	@Override
	public List<MusicSet> getMusicSets(final String chatId) {
		final MongoCollection<Document> collection = getRemoteContentCollection(chatId);
		final MongoCursor<Document> cursor = collection.find().iterator();
		
		final List<MusicSet> sets = Lists.newArrayList();
		while (cursor.hasNext()) {
			final Document doc = cursor.next();
			final RemoteContent content = RemoteContent.getRemoteContentFromDoc(doc);
			
			// Only return remote content that has been detected as a Music Set
			if (ContentType.isMusicSet(content.type)) {
				sets.add(MusicSet.getSetFromDoc(doc));
			}
		}
		return sets;
	}
	
	@Override
	public long getRemoteContentCount(String chatId) {
		return getRemoteContentCollection(chatId).count();
	}

	@Override
	public void insertRemoteContent(String chatId, List<DetectedContent> remoteContent) {
		List<Document> remoteContentDocumentsToInsert = Lists.newArrayList();
		for (final DetectedContent content : remoteContent) {
			try {
				final Document user = new Document().append("userId", content.user.userId)
						.append("firstName", content.user.firstName)
						.append("lastName", content.user.lastName);
				
				final Document newSet = new Document("_id", new ObjectId());
				newSet.append("url", content.url);
				newSet.append("type", content.type.name());
				newSet.append("originalDate", content.date);
				newSet.append("originalUser", user);
				newSet.append("references", asList());
				remoteContentDocumentsToInsert.add(newSet);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		getRemoteContentCollection(chatId).insertMany(remoteContentDocumentsToInsert);
	}
	
	@Override
	public void updateRemoteContentType(final String chatId, final String id, ContentType type) {
		final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(RemoteContent.FIELD_TYPE, type.name()));
		try {
			final UpdateResult result = getRemoteContentCollection(chatId).updateOne(eq("_id", new ObjectId(id)), update);
			if (result.getMatchedCount() != 1) {
				logger.error("Update error!");
			}
		} catch (final Exception e) {
			logger.error("Error encountered updating type!", e);
		}
	}
	
	@Override
	public void updateSetStatus(final String chatId, final MusicSet set, final MusicSet.Status status) {
		final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(MusicSet.FIELD_STATUS, status.name()));
		try {
			final UpdateResult result = getRemoteContentCollection(chatId).updateOne(eq(MusicSet.FIELD_URL, set.url), update);
			if (result.getMatchedCount() != 1 && result.getModifiedCount() != 1) {
				logger.error("Could not find or update set status! [chatId: " + chatId + " , url: " + set.url + "]");
			}
		} catch (Exception e) {
			logger.error("Error encountered updating status!", e);
		}
	}
	
	@Override
	public void updateSetMetadata(final String chatId, final MusicSet set, final MusicSet.Metadata metadata) {
		final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(MusicSet.FIELD_METADATA, metadata.toDoc()));
		final UpdateResult result = getRemoteContentCollection(chatId).updateOne(eq(MusicSet.FIELD_URL, set.url), update);
		
		if (result.getMatchedCount() != 1 && result.getModifiedCount() != 1) {
			logger.error("Could not find or update set metadata! [chatId: " + chatId + " , url: " + set.url + "]");
		}
	}

	@Override
	public void updateRemoteContentReference(String chatId, DetectedContent set) {
		try {
			final Document user = new Document().append("userId", set.user.userId)
					.append("firstName", set.user.firstName)
					.append("lastName", set.user.lastName);
			
			final Document newReference = new Document("user", user);
			newReference.append("date", set.date);
			
			final BasicDBObject update = new BasicDBObject("$push", new BasicDBObject("references", newReference));
			getRemoteContentCollection(chatId).updateOne(eq(SET_URL_BSON, set.url), update);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public void addPlayToMusicSet(final String chatId, final String setId, final int userId) {
		try {
			final Play newPlay = new Play();
			newPlay.userId = userId;
			// since we're using Javascript dates everywhere, be consistent here
			newPlay.dateOfPlay = (int) (new Date()).getTime() / 1000; 
			
			final Document newPlayDoc = newPlay.toDoc();
			
			final BasicDBObject update = new BasicDBObject("$push", new BasicDBObject(MusicSet.FIELD_PLAYS, newPlayDoc));
			final UpdateResult result = getRemoteContentCollection(chatId).updateOne(eq("_id", new ObjectId(setId)), update);
			if (result.getMatchedCount() != 1) {
				throw new Exception("couldn't match!");
			}
		} catch (final Exception e) {
			logger.error("Could not update play count! [chatId: " + chatId + " , set: " + setId + "]");
		}
	}
	
	public List<TermResponse> getTermResponses(String chatId) {
		final List<TermResponse> termResponses = Lists.newArrayList();
		try {
			MongoCursor<Document>  cursor = getAutoResponseCollection(chatId).find().iterator();
			while(cursor.hasNext()) {
				Document doc = cursor.next();
				termResponses.add(TermResponse.getTermResponseFromDoc(doc));
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return termResponses;
	}
	
	@Override
	public void insertTermResponse(final String chatId, final TermResponse termResponse) {
		try {
			final Document termResponseDoc = new Document().append("userId", termResponse.userId)
					.append("date", termResponse.date)
					.append("term", termResponse.term)
					.append("response", termResponse.response);

			getAutoResponseCollection(chatId).insertOne(termResponseDoc);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public void deleteTermResponse(final String chatId, TermResponse termResponse) {
		try {
			DeleteResult result = getAutoResponseCollection(chatId).deleteOne(eq("_id", new ObjectId(termResponse._id)));
			if (result.getDeletedCount() != 1) {
				logger.warn("Delete problem!");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

}
