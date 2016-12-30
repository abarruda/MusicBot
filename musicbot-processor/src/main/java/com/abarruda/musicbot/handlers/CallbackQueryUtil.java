package com.abarruda.musicbot.handlers;

import org.telegram.telegrambots.api.objects.CallbackQuery;

public class CallbackQueryUtil {
	
	public static final String DELIMITER = ":::";
	
	public static class CallbackQueryInfo {
		public final String source;
		public final String data;
		
		private CallbackQueryInfo(final String source, final String data) {
			this.source = source;
			this.data = data;
		}
	}
	
	public static CallbackQueryInfo getInfo(final CallbackQuery query) {
		final String data = query.getData();
		final String[] splitData = data.split(DELIMITER);
		if (splitData.length != 2) {
			throw new IllegalArgumentException("Invalid data in Callbackquery: " + query.getData());
		}
		final CallbackQueryInfo info = new CallbackQueryInfo(splitData[0], splitData[1]);
		return info;
	}

}
