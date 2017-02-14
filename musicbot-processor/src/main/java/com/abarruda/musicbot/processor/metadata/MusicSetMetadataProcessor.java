package com.abarruda.musicbot.processor.metadata;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

public class MusicSetMetadataProcessor {
	
	private static final Logger logger = LogManager.getLogger(MusicSetMetadataProcessor.class);
	
	private final MetadataScraper.Factory metadataScraperFactory;
	private final ScheduledExecutorService executor;
	
	@Inject
	public MusicSetMetadataProcessor(final MetadataScraper.Factory metadataScraperFactory) {
		this.metadataScraperFactory = metadataScraperFactory;
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("MusicSet-Metadata-thread-%d").build();
		executor = Executors.newScheduledThreadPool(2, threadFactory);
	}
	
	public void start() {
		logger.info("Scheduled metadata processing.");
		// Schedule thread for sets without metadata
		final MetadataScraper newSetScraper = this.metadataScraperFactory.create(false, 5, 0);
		executor.scheduleAtFixedRate(newSetScraper, 10, 5, TimeUnit.SECONDS);
		// schedule thread for sets with metadata that may need to be updated
		final MetadataScraper allSetsMaintainerScraper = this.metadataScraperFactory.create(true, 2, 2);
		executor.scheduleAtFixedRate(allSetsMaintainerScraper, 12, 24, TimeUnit.HOURS);
	}

}
