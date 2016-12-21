package com.abarruda.musicbot.processor.item;

import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.items.ContentType;
import com.abarruda.musicbot.items.RemoteContent;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * This class represents a process to iterate over RemoteContent
 * and perform operations due to changes in the data model.
 */
public class ItemValidator implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(ItemValidator.class);

	private final DatabaseFacade db;
	private final Predicate<RemoteContent> filterPredicate;
	private final Function<RemoteContent, RemoteContent> function;
	
	private ItemValidator(final Predicate<RemoteContent> filterPredicate, Function<RemoteContent, RemoteContent> function) {
		this.db = MongoDbFacade.getMongoDb();
		this.filterPredicate = filterPredicate;
		this.function = function;
	}
	
	public static void updateUnknownTypesToKnownTypes() {
		final Predicate<RemoteContent> predicate = new Predicate<RemoteContent>() {
			
			@Override
			public boolean apply(RemoteContent input) {
				return input.type == null || input.type.isEmpty();
			}
		};
		
		final Function<RemoteContent, RemoteContent> function = new Function<RemoteContent, RemoteContent>() {
			
			@Override
			public RemoteContent apply(RemoteContent input) {
				final ContentType newType = ContentType.determineContentType(input.url);
				input.type = newType.name();
				logger.info("Updating item with URL '" + input.url + "' with type '" + newType.name() + "'.");
				return input;
			}
		};
		
		final ItemValidator validator = new ItemValidator(predicate, function);
		new Thread(validator).start();
	}
	
	@Override
	public void run() {
		try {
			for (final String chatId : db.getChatIds().keySet()) {
				db.getRemoteContent(chatId);
				final Set<RemoteContent> content = db.getRemoteContent(chatId);
				final Iterable<RemoteContent> filtered = Iterables.filter(content, filterPredicate);
				final Iterable<RemoteContent> updated = Iterables.transform(filtered, function);
				for (final RemoteContent updatedContent : updated) {
					db.updateRemoteContentType(chatId, updatedContent._id, ContentType.valueOf(updatedContent.type));
				}
			}
			logger.info("Validation complete.");
		} catch (final Exception e) {
			logger.error("Can not run validator!", e);
		}
		
	}

}
