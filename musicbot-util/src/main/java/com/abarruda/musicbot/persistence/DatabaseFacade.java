package com.abarruda.musicbot.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abarruda.musicbot.items.Chat;
import com.abarruda.musicbot.items.ContentType;
import com.abarruda.musicbot.items.DetectedContent;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.items.User;

public interface DatabaseFacade {
	
	Map<String, String> getChatIds();
	
	public Chat getChatByTelegramId(final String chatId);
	
	public void storeChat(final String chatId, final String chatName);
	
	public boolean deleteChatId(final String chatId);
	
	public void updateLastSeen(final String chatId, final Integer userId, final String firstName, final String lastName, final String dateString);
	
	public Set<User> getUsersForChat(final String chatId);
	
	public String getUserLastSeenFromChat(final String chatId, final int userId);
	
	public void userExpired(final String chatId, final int userId);
	
	public Set<RemoteContent> getRemoteContent(final String chatId);
	
	public RemoteContent getRemoteContent(final String chatId, final String url);
	
	public Map<MusicSet, String> getMusicSets();
	
	public long getRemoteContentCount(final String chatId);
	
	public void insertRemoteContent(final String chatId, final List<DetectedContent> sets);
	
	public void updateRemoteContentType(final String chatId, final String id, ContentType type);
	
	public void updateSetStatus(final String chatId, final MusicSet set, final MusicSet.Status status);
	
	public void updateSetMetadata(final String chatId, final MusicSet set, final MusicSet.Metadata metadata);
	
	public void updateRemoteContentReference(final String chatId, final DetectedContent set);
	
	public List<MusicSet> getMusicSets(final String chatId);
	
	public void addPlayToMusicSet(final String chatId, final String setId, final int userId);
	
	public List<TermResponse> getTermResponses(final String chatId);
	
	public void insertTermResponse(final String chatId, final TermResponse termResponse);
	
	public void deleteTermResponse(final String chatId, final TermResponse termResponse);

}
