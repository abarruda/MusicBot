package com.abarruda.musicbot.api.sets.v1.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.api.sets.v1.resource.SetsResource;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.items.SetType;
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
			Iterables.concat(SetType.SOUNDCLOUD.hostNames, SetType.YOUTUBE.hostNames));

	private static final Predicate<RemoteContent> setPredicate = new Predicate<RemoteContent>() {
		@Override
		public boolean apply(RemoteContent input) {
			for (final String hostname : setUrls) {
				if (input.url.contains(hostname))
					return true;
			}
			return false;
		}
	};
	
	private static final Comparator<RemoteContent> setOrdering = new Comparator<RemoteContent>() {
		@Override
		public int compare(RemoteContent left, RemoteContent right) {
			return Ints.compare(left.references.size(), right.references.size());
		}
	}; 
	
	@Override
	public Iterable<RemoteContent> getSetsByChatId(final String id, final String userId, final boolean orderByReferenceCount) {
		
		final List<Predicate<RemoteContent>> listOfPredicates = new ArrayList<Predicate<RemoteContent>>();
		listOfPredicates.add(setPredicate);
		
		if (userId != null) {
			listOfPredicates.add(new Predicate<RemoteContent>() {
				@Override
				public boolean apply(RemoteContent input) {
					return String.valueOf(input.originalUser.userId).equals(userId);
				}
			});
		}
		
		final DatabaseFacade db = MongoDbFacade.getMongoDb();
		final List<RemoteContent> remoteContent = db.getRemoteContent(id);
		
		final List<RemoteContent> filteredRemoteContent = Lists.newLinkedList(
				Iterables.filter(remoteContent, 
						Predicates.and(listOfPredicates)));
		
		if (orderByReferenceCount) {
			Collections.sort(filteredRemoteContent, Ordering.from(setOrdering).reversed()); 
		}
		
		return filteredRemoteContent;
	}
	
}
