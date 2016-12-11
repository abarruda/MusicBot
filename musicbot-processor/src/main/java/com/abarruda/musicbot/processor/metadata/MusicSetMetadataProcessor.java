package com.abarruda.musicbot.processor.metadata;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class MusicSetMetadataProcessor {
	
	private static final Logger logger = LogManager.getLogger(MusicSetMetadataProcessor.class);
	
	private ScheduledExecutorService executor;
	
	public MusicSetMetadataProcessor() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("MusicSet-Metadata-thread-%d").build();
		executor = Executors.newScheduledThreadPool(2, threadFactory);
	}
	
	public void start() {
		// Schedule thread for sets without metadata
		executor.scheduleAtFixedRate(MetadataScraper.doOnlyNewSets(), 30, 30, TimeUnit.SECONDS);
		// schedule thread for sets with metadata that may need to be updated
		executor.scheduleAtFixedRate(MetadataScraper.doAllSets(), 12, 24, TimeUnit.HOURS);
	}

}
