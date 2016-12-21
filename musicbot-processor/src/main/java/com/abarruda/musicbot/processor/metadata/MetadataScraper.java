package com.abarruda.musicbot.processor.metadata;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.abarruda.musicbot.items.MusicSet;
import com.abarruda.musicbot.items.MusicSet.Metadata;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class MetadataScraper implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(MetadataScraper.class);
	
	private final boolean allSets;
	private final int delaySeconds;
	private DatabaseFacade db;
	private ExecutorService executor;
	
	
	private MetadataScraper(final boolean allSets, final int threadPoolSize, final int delaySeconds) {
		this.allSets = allSets;
		this.delaySeconds = delaySeconds;
		this.db = MongoDbFacade.getMongoDb();
		
		final String type = allSets ? "AllSets" : "NewSets";
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("MusicSet-Metadata-" + type + "-thread-%d").build();
		executor = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
	}
	
	private static String getPropertyFromDoc(final Document doc, final String property) {
		final Elements elements = doc.getElementsByAttributeValue("property", property);
		if (elements.size() > 1) {
			logger.warn("Found more than property for '" + property + "'!  Using first property.");
		} else if (elements.isEmpty()) {
			logger.warn("Did not find property '" + property + "'!");
			return null;
		}
		
		return elements.first().attr("content");
	}
	
	private static MusicSet.Status getStatusOfSet(final Document doc) {
		if (doc == null || getPropertyFromDoc(doc, "og:title") == null) {
			return MusicSet.Status.INACTIVE;
		}
		return MusicSet.Status.ACTIVE;
	}
	
	private static MusicSet.Metadata getMetadata(final Document doc) {
		final Metadata metadata = new Metadata();
		metadata.title = getPropertyFromDoc(doc, "og:title");
		metadata.imageUrl = getPropertyFromDoc(doc, "og:image");
		return metadata;
	}
	
	private final static class MetadataScraperWorker implements Runnable {
		
		private final boolean allSets;
		private final String chatId;
		private final MusicSet set;
		private int sleepSeconds;
		private DatabaseFacade db;
		
		private MetadataScraperWorker(final boolean allSets, final String chatId, 
				final MusicSet set, final int sleepSeconds) {
			this.allSets = allSets;
			this.chatId = chatId;
			this.set = set;
			this.db = MongoDbFacade.getMongoDb();
		}

		@Override
		public void run() {
			if (
				(this.set.metadata == null && this.set.status == null) // Sets that don't have metadata and are new (status hasn't been determined) 
				|| this.allSets // OR all sets
					) {
				logger.info("Processing metadata for " + this.set.url);
				
				Document doc = null;
				try {
					doc = Jsoup.connect(this.set.url).get();
				} catch (IOException e) {
					logger.warn("Unable to retrieve " + set.url + " !");
				}
				
				if (getStatusOfSet(doc) == MusicSet.Status.ACTIVE) {
					final MusicSet.Metadata metadata = getMetadata(doc);
					logger.info("Retrieved metadata for " + this .set.url + ": " + metadata.toDoc().toJson());
					// update metadata
					db.updateSetMetadata(this.chatId, this.set, metadata);
					db.updateSetStatus(chatId, set, MusicSet.Status.ACTIVE);
				} else {
					logger.info("Set is INACTIVE: " + set.url);
					db.updateSetStatus(chatId, set, MusicSet.Status.INACTIVE);
				}
				
				try {
					TimeUnit.SECONDS.sleep(sleepSeconds);
				} catch (InterruptedException e) {
					logger.error("Processing " + set.url + " interrupted!");
				}
			}
			
		}
	}
	
	public void run() {
		try {
			final Map<MusicSet, String> allSets = db.getSets();
			for (Entry<MusicSet, String> entry : allSets.entrySet()) {
				executor.submit(
						new MetadataScraperWorker(this.allSets, entry.getValue(), entry.getKey(),  delaySeconds));
			}
		} catch (final Exception e) {
			logger.fatal("Cannot run metadata processor!", e);
		}
		
	}
	
	public static MetadataScraper doAllSets() {
		return new MetadataScraper(true, 2, 2);
	}
	
	public static MetadataScraper doOnlyNewSets() {
		return new MetadataScraper(false, 5, 0);
	}
	

}
