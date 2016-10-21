package com.abarruda.musicbot.persistence;

import java.util.List;
import java.util.Map;

import com.abarruda.musicbot.items.DetectedSet;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.items.TermResponse;

public interface DatabaseFacade {
	
	Map<String, String> getChatIds();
	
	public void storeChat(final String chatId, final String chatName);
	
	public boolean deleteChatId(final String chatId);
	
	public void updateLastSeen(final String chatId, final String userId, final String dateString);
	
	public String getUserLastSeenFromChat(final String chatId, final String userId);
	
	public void userExpired(final String chatId, final String userId);
	
	public MusicSet getSet(final String chatId, final String url);
	
	public long getSetCount(final String chatId);
	
	public void insertSets(final String chatId, final List<DetectedSet> sets);
	
	public void updateSetReference(final String chatId, final DetectedSet set);
	
	public List<RemoteContent> getRemoteContent(final String chatId);
	
	public List<TermResponse> getTermResponses(final String chatId);
	
	public void insertTermResponse(final String chatId, final TermResponse termResponse);
	
	public void deleteTermResponse(final String chatId, final TermResponse termResponse);

}
