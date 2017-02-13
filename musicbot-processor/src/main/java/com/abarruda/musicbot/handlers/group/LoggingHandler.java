package com.abarruda.musicbot.handlers.group;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.config.Config;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.eventbus.Subscribe;

public class LoggingHandler {
	private static final Logger logger = LogManager.getLogger(LoggingHandler.class);
	
	private static final String LOG_FILE_DIR = Config.getConfig(Config.LOGGING_FILE_LOCATION);
	
	private LoadingCache<String, FileWriter> logFileCache;
	
	private static CacheLoader<String, FileWriter> logFileCacheLoader = new CacheLoader<String, FileWriter>() {
		@Override
		public FileWriter load(String chatId) throws Exception {
			final File logFileDir = new File(LOG_FILE_DIR);
			
			if (!logFileDir.exists() || logFileDir.mkdirs()) {
				throw new IllegalStateException("Cannot find or create logging directory!");
			}
			Preconditions.checkArgument(logFileDir.exists() && logFileDir.isDirectory(), "Logging files directory doesn't exist!");
			
			final File[] logFiles = logFileDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().equals(chatId);
				}
			});
			
			final File logFile;
			if (logFiles.length == 0) {
				final File newFile = new File(LOG_FILE_DIR + File.separator + chatId);
				newFile.createNewFile();
				logFile = newFile;
			} else if (logFiles.length == 1) {
				logFile = logFiles[0];
			} else {
				throw new IllegalStateException("More than one log file for chat " + chatId + "!");
				
			}
			
			final FileWriter logFileWriter = new FileWriter(logFile);
			return logFileWriter;
		}
	};
	
	private static RemovalListener<String, FileWriter> removalListener = new RemovalListener<String, FileWriter>() {
		@Override
		public void onRemoval(RemovalNotification<String, FileWriter> notification) {
			try {
				notification.getValue().close();
			} catch (IOException e) {
				logger.error("Unable to close file writer!");
			}
		}
	};
	
	public LoggingHandler() {
		
		logFileCache = CacheBuilder.newBuilder()
				.maximumSize(100)
				.expireAfterAccess(7, TimeUnit.DAYS)
				.removalListener(removalListener)
				.build(logFileCacheLoader);
	}
	
	private void writeMessageToLog(final Message message) throws IOException, ExecutionException {
		final String chatId = message.getChatId().toString();
		final FileWriter fileWriter = logFileCache.get(chatId);
		final StringBuilder sb = new StringBuilder();
		
		// Long time = Long.valueOf(message.getDate()); For some reason telegram reports back an invalid date
		sb.append(new Date());
		sb.append(" - ");
		sb.append("(");
		sb.append(message.getFrom().getId());
		sb.append(")");
		sb.append(" - ");
		sb.append(message.getFrom().getFirstName());
		sb.append(" ");
		sb.append(message.getFrom().getLastName());
		sb.append(": ");
		sb.append(message.getText());
		sb.append("\n");
		try {
			fileWriter.write(sb.toString());
			fileWriter.flush();
		} catch (final Exception e) {
			logFileCache.invalidate(chatId);
		}
	}

	@Subscribe
	public void handleMessage(Message message) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				if (message.hasText()) {
					try {
						writeMessageToLog(message);
					} catch (final Exception e) {
						logger.error("Unable to log message for chat " + message.getChatId().toString() + "!", e);
					}
				}
				
			}
		}).start();
		
	}

}
