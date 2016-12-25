package com.abarruda.musicbot;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Maps;

public class ChatManager {
	
	private static final int USER_EXPIRATION_DAYS = 7;
	
	private DatabaseFacade db;
	private Map<String, LoadingCache<String, String>> cacheMap;
	private Map<String, String> chatIdToChatNameMapping;
	
	private static ChatManager chatManager;
	
	private ChatManager() {
		cacheMap = Maps.newHashMap();
		chatIdToChatNameMapping = Maps.newHashMap();
		db = MongoDbFacade.getMongoDb();
		loadCacheFromDb();
	}
	
	private RemovalListener<String, String> getCacheRemovalListener(String chatId) {
		final RemovalListener<String, String> listener = new RemovalListener<String, String>() {
			@Override
			public void onRemoval(RemovalNotification<String, String> notification) {
				db.userExpired(chatId, notification.getKey());
			}
		};
		return listener;
	}
	
	@SuppressWarnings("serial")
	public static class UserLastSeenNotFoundException extends Exception {};
	
	private LoadingCache<String, String> getNewCacheForChat(String chatId) {
		final LoadingCache<String, String> lastSeenCache = CacheBuilder.newBuilder()
				.expireAfterWrite(USER_EXPIRATION_DAYS, TimeUnit.DAYS)
				.removalListener(getCacheRemovalListener(chatId))
				.build(
						new CacheLoader<String, String>() {

							@Override
							public String load(String key) throws Exception {
								final String lastSeen = db.getUserLastSeenFromChat(chatId, Integer.valueOf(key));
								if (lastSeen == null) {
									throw new UserLastSeenNotFoundException();
								}
								return lastSeen;
							}
							
						});
		return lastSeenCache;
	}
	
	private void loadCacheFromDb() {
		for (Map.Entry<String, String> chatIdAndName : db.getChatIds().entrySet()) {
			final String chatId = chatIdAndName.getKey();
			final String chatName = chatIdAndName.getValue();
			cacheMap.put(chatId, getNewCacheForChat(chatId));
			chatIdToChatNameMapping.put(chatId, chatName);
		}
	}
	
	public static ChatManager getChatManager() {
		if (chatManager == null) {
			chatManager = new ChatManager();
		}
		return chatManager;
	}
	
	public void update(final String chatId, final String chatName, final Integer userId, final String firstName, final String lastName, final String dateString) {
		chatIdToChatNameMapping.put(chatId, chatName);
		db.storeChat(chatId, chatName);
		
		if (!cacheMap.containsKey(chatId)) {
			final LoadingCache<String, String> newCache = getNewCacheForChat(chatId);
			cacheMap.put(chatId, newCache);
		}
		
		cacheMap.get(chatId).put(userId.toString(), dateString);
		db.updateLastSeen(chatId, userId, firstName, lastName, dateString);
	}
	
	/**
	 * Returns the chats (chatName=>chatId) the user has been last seen in within a specific range
	 * 
	 */
	public Map<String, String> getChatsForUserFromCache(Integer userId) {
		
		final Map<String, String> chatsForUser = Maps.newHashMap();
		
		for (Map.Entry<String, LoadingCache<String, String>> cacheEntry : cacheMap.entrySet()) {
			final LoadingCache<String, String> cache = cacheEntry.getValue();
			
			String userLastSeen;
			try {
				userLastSeen = cache.get(userId.toString());//cache.getIfPresent(userId);
			} catch (ExecutionException e) {
				userLastSeen = null;
			}
			
			if (userLastSeen != null && lastSeenWithinWithinRange(userLastSeen)) {
				final String chatId = cacheEntry.getKey();
				chatsForUser.put(chatIdToChatNameMapping.get(chatId), chatId);
				
			} else {
				cache.invalidate(userId);
			}
		}
		
		return chatsForUser;
	}
	
	private boolean lastSeenWithinWithinRange(String dateString) {
		final Date lastSeen = new Date(Long.valueOf(dateString) * 1000L);
		return (System.currentTimeMillis() - lastSeen.getTime()) < Duration.ofDays(USER_EXPIRATION_DAYS).toMillis();
	}

}
