package com.abarruda.musicbot.api.sets.v1.container;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.abarruda.musicbot.items.ContentType;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.MusicSet.Play;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.items.User;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.google.common.collect.Lists;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SetsContainerTest {
	
	@Mock private DatabaseFacade mockDb;
	private SetsContainer container;
	
	@Before
	public void setUp() {
		container = new SetsContainer(mockDb);
	}
	
	private Play newPlay() {
		final Play play = new Play();
		play.userId = 1;
		play.dateOfPlay = 1;
		return play;
	}
	
	@Test
	public void testsOrderPopularByMostRecent() {
		final String chatId = "chat-id";
		
		final User user1 = new User(1, "test", "user");
		final RemoteContent content1 = new RemoteContent("remote-content-id-1", "http://" + ContentType.SOUNDCLOUD.hostNames.get(0) + "/1", "SOUNDCLOUD", user1, 1, Lists.newArrayList());
		final MusicSet set1 = new MusicSet(content1, "ACTIVE", null, Lists.newArrayList(newPlay(), newPlay(), newPlay(), newPlay()));
		
		final User user2 = new User(2, "test", "user");
		final RemoteContent content2 = new RemoteContent("remote-content-id-2", "http://" + ContentType.SOUNDCLOUD.hostNames.get(1) + "/2", "SOUNDCLOUD", user2, 100, Lists.newArrayList());
		final MusicSet set2 = new MusicSet(content2, "ACTIVE", null, Lists.newArrayList(newPlay(), newPlay(), newPlay()));
		
		final User user3 = new User(1, "test", "user");
		final RemoteContent content3 = new RemoteContent("remote-content-id-3", "http://" + ContentType.SOUNDCLOUD.hostNames.get(2) + "/3", "SOUNDCLOUD", user3, 2000, Lists.newArrayList());
		final MusicSet set3 = new MusicSet(content3, "ACTIVE", null, Lists.newArrayList(newPlay(), newPlay()));
		
		final User user4 = new User(4, "test", "user");
		final RemoteContent content4 = new RemoteContent("remote-content-id-4", "http://" + ContentType.YOUTUBE.hostNames.get(1) + "/4", "SOUNDCLOUD", user4, 50, Lists.newArrayList());
		final MusicSet set4 = new MusicSet(content4, "ACTIVE", null, Lists.newArrayList(newPlay(), newPlay(), newPlay(), newPlay(), newPlay()));
		
		when(mockDb.getMusicSets(chatId)).thenReturn(Lists.newArrayList(set3, set1, set2, set4));
		
		final Iterable<MusicSet> sets = container.getPopularSetsByChatId(chatId, null);
		
		final Iterator<MusicSet> sortedSetsIterator = sets.iterator();
		assertEquals("remote-content-id-2", sortedSetsIterator.next()._id);
		assertEquals("remote-content-id-4", sortedSetsIterator.next()._id);
		assertEquals("remote-content-id-1", sortedSetsIterator.next()._id);
	}

}
