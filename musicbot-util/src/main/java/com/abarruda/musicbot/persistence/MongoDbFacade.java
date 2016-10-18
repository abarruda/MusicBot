package com.abarruda.musicbot.persistence;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.abarruda.musicbot.config.Config;
import com.abarruda.musicbot.items.DetectedSet;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.TermResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
	public void updateLastSeen(String chatId, String userId, String dateString) {
		final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(LAST_SEEN_BSON, dateString));
		final UpdateResult result = getChatMemberCollection(chatId).updateOne(
				eq(USER_ID_BSON, userId), 
				update, 
				new UpdateOptions().upsert(true));
		
		if (result.getMatchedCount() != 1 && result.getUpsertedId() == null) {
			logger.error("Error updating last seen.");
		}
	}

	@Override
	public String getUserLastSeenFromChat(String chatId, String userId) {
		final MongoCollection<Document> collection = getChatMemberCollection(chatId);
		final Document lastSeen = collection.find(eq(USER_ID_BSON, userId)).first();
		if (lastSeen != null) {
			return lastSeen.getString(LAST_SEEN_BSON);
		} else {
			return null;
		}
	}

	@Override
	public void userExpired(String chatId, String userId) {
		DeleteResult result = getChatMemberCollection(chatId).deleteOne(eq(USER_ID_BSON, userId));
		if (result.getDeletedCount() != 1) {
			logger.warn("Error deleting expired user!");
		}
	}

	@Override
	public MusicSet getSet(final String chatId, final String url) {
		
		final Document dbDoc = getRemoteContentCollection(chatId).find(eq(SET_URL_BSON, url)).first();
		if (dbDoc == null) {
			return null;
		} else {
			return MusicSet.getSetFromDoc(dbDoc);
		}
	}
	
	@Override
	public long getSetCount(String chatId) {
		return getRemoteContentCollection(chatId).count();
	}

	@Override
	public void insertSets(String chatId, List<DetectedSet> sets) {
		List<Document> setDocumentsToBeInserted = Lists.newArrayList();
		for (final DetectedSet set : sets) {
			try {
				final Document user = new Document().append("userId", set.user.getId())
						.append("firstName", set.user.getFirstName())
						.append("lastName", set.user.getLastName());
				
				final Document newSet = new Document("_id", new ObjectId());
				newSet.append("url", set.url);
				newSet.append("originalDate", set.date);
				newSet.append("originalUser", user);
				newSet.append("references", asList());
				setDocumentsToBeInserted.add(newSet);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		getRemoteContentCollection(chatId).insertMany(setDocumentsToBeInserted);
	}

	@Override
	public void updateSetReference(String chatId, DetectedSet set) {
		try {
			final Document user = new Document().append("userId", set.user.getId())
					.append("firstName", set.user.getFirstName())
					.append("lastName", set.user.getLastName());
			
			final Document newReference = new Document("user", user);
			newReference.append("date", set.date);
			
			final BasicDBObject update = new BasicDBObject("$push", new BasicDBObject("references", newReference));
			getRemoteContentCollection(chatId).updateOne(eq(SET_URL_BSON, set.url), update);
		} catch (Exception e) {
			logger.error(e);
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