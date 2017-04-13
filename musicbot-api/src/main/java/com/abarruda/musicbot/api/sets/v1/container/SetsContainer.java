package com.abarruda.musicbot.api.sets.v1.container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.api.sets.v1.resource.SetsResource;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.ContentType;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

public class SetsContainer extends SetsResource {
	private static final Logger logger = LogManager.getLogger(SetsContainer.class);
	
	final DatabaseFacade db;
	
	@Inject
	public SetsContainer(final DatabaseFacade db) {
		this.db = db;
	}
	
	private static ImmutableSet<String> setUrls = ImmutableSet.copyOf(
			Iterables.concat(ContentType.SOUNDCLOUD.hostNames, ContentType.YOUTUBE.hostNames));

	private static final Predicate<MusicSet> setPredicate = new Predicate<MusicSet>() {
		@Override
		public boolean apply(MusicSet input) {
			for (final String hostname : setUrls) {
				if (input.url.contains(hostname))
					return true;
			}
			return false;
		}
	};
	
	private static final Predicate<MusicSet> activeSetPredicate = new Predicate<MusicSet>() {

		@Override
		public boolean apply(MusicSet input) {
			return input.status.equals(MusicSet.Status.ACTIVE.name());
		}
	};
	
	private static final Predicate<MusicSet> popularSetPredicate = new Predicate<MusicSet>() {
		@Override
		public boolean apply(MusicSet input) {
			return (input.references.size() > 2) || input.plays.size() > 2;
		}
	};
	
	private static final Comparator<MusicSet> orderByReferences = new Comparator<MusicSet>() {
		@Override
		public int compare(MusicSet left, MusicSet right) {
			return Ints.compare(left.references.size(), right.references.size());
		}
	};
	
	private static final Comparator<MusicSet> orderByMostRecent = new Comparator<MusicSet>() {

		@Override
		public int compare(MusicSet left, MusicSet right) {
			return Ints.compare(right.originalDate, left.originalDate);
		}
	};
	
	private static Predicate<MusicSet> getUserSetPredicate(final String userId) {
		return new Predicate<MusicSet>() {
			@Override
			public boolean apply(MusicSet input) {
				return String.valueOf(input.originalUser.userId).equals(userId);
			}
		};
	}
	
	@Override
	public Iterable<MusicSet> getPopularSetsByChatId(final String id, final String userId) {
		
		final List<Predicate<MusicSet>> listOfPredicates = new ArrayList<Predicate<MusicSet>>();
		listOfPredicates.add(setPredicate);
		listOfPredicates.add(activeSetPredicate);
		listOfPredicates.add(popularSetPredicate);
		
		if (userId != null) {
			listOfPredicates.add(getUserSetPredicate(userId));
		}
		
		final List<MusicSet> musicSets = db.getMusicSets(id);
		
		final List<MusicSet> filteredMusicSets = Lists.newLinkedList(
				Iterables.filter(musicSets, 
						Predicates.and(listOfPredicates)));
		
		Collections.sort(filteredMusicSets, Ordering.from(orderByMostRecent)); 
		
		return filteredMusicSets;
	}
	
	@Override
	public Iterable<MusicSet> getRecentSetsByChatId(final String id, final String durationDaysString, final String user) {
		final List<Predicate<MusicSet>> listOfPredicates = new ArrayList<Predicate<MusicSet>>();
		listOfPredicates.add(activeSetPredicate);
		
		listOfPredicates.add(new Predicate<MusicSet>() {
			@Override
			public boolean apply(MusicSet input) {
				final Date originalDate = new Date(input.originalDate * 1000L);
				long timeSinceFirstSeen = new Date().getTime() - originalDate.getTime();
				return timeSinceFirstSeen <= Duration.parse(durationDaysString).toMillis();
			}
		});
		
		if (user != null) {
			listOfPredicates.add(getUserSetPredicate(user));
		}
		
		final List<MusicSet> musicSets = db.getMusicSets(id);
		
		final List<MusicSet> filteredMusicSets = Lists.newLinkedList(
				Iterables.filter(musicSets, 
						Predicates.and(listOfPredicates)));
		
		Collections.sort(filteredMusicSets, Ordering.from(orderByMostRecent));
		return filteredMusicSets;
	}
	
	@Override public Iterable<MusicSet> getBrowsingSetsByChatId(final String chatId, final String userId) {
		final List<Predicate<MusicSet>> listOfPredicates = new ArrayList<Predicate<MusicSet>>();
		listOfPredicates.add(setPredicate);
		listOfPredicates.add(activeSetPredicate);
		
		if (userId != null) {
			listOfPredicates.add(getUserSetPredicate(userId));
		}
		
		final List<MusicSet> musicSets = db.getMusicSets(chatId);
		
		final List<MusicSet> filteredMusicSets = Lists.newLinkedList(
				Iterables.filter(musicSets, 
						Predicates.and(listOfPredicates)));
		
		Collections.sort(filteredMusicSets, Ordering.from(orderByMostRecent));
		return filteredMusicSets;
	}

	@Override
	public void playSet(String chatId, String setId, String userId) {
		logger.info("Set '" + setId + "' from chat '" + chatId + "' being played by '" + userId + "'.");
		db.addPlayToMusicSet(chatId, setId, Integer.valueOf(userId));
	}
	
}
