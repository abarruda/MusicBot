package com.abarruda.musicbot.api.sets.v1.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.api.sets.v1.resource.SetsResource;
import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.ContentType;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class SetsContainer extends SetsResource {
	private static final Logger logger = LogManager.getLogger(SetsContainer.class);
	
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
	
	private static final Comparator<MusicSet> setOrdering = new Comparator<MusicSet>() {
		@Override
		public int compare(MusicSet left, MusicSet right) {
			return Ints.compare(left.references.size(), right.references.size());
		}
	}; 
	
	@Override
	public Iterable<MusicSet> getSetsByChatId(final String id, final String userId, final boolean orderByReferenceCount) {
		
		final List<Predicate<MusicSet>> listOfPredicates = new ArrayList<Predicate<MusicSet>>();
		listOfPredicates.add(setPredicate);
		
		if (userId != null) {
			listOfPredicates.add(new Predicate<MusicSet>() {
				@Override
				public boolean apply(MusicSet input) {
					return String.valueOf(input.originalUser.userId).equals(userId);
				}
			});
		}
		
		final DatabaseFacade db = MongoDbFacade.getMongoDb();
		final List<MusicSet> musicSets = db.getMusicSets(id);
		
		final List<MusicSet> filteredMusicSets = Lists.newLinkedList(
				Iterables.filter(musicSets, 
						Predicates.and(listOfPredicates)));
		
		if (orderByReferenceCount) {
			Collections.sort(filteredMusicSets, Ordering.from(setOrdering).reversed()); 
		}
		
		return filteredMusicSets;
	}
	
}
