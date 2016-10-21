package com.abarruda.musicbot.api.sets.v1.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.abarruda.musicbot.api.sets.v1.resource.SetsResource;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.items.SetType;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class SetsContainer extends SetsResource {
	
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
	
	@Override
	public Iterable<RemoteContent> getSetsByChatId(final String id, final String userId) {
		
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
		final Set<RemoteContent> remoteContent = db.getRemoteContent(id);
		return Iterables.filter(remoteContent, Predicates.and(listOfPredicates));
	}
	
}
