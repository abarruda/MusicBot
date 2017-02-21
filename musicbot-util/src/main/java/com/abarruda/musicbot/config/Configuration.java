package com.abarruda.musicbot.config;

import java.util.Properties;

public class Configuration {
	
	public static String API_PORT = "api.port";
	public static String BOT_NAME = "bot.name";
	public static String API_TOKEN = "api.token";
	public static String DATABASE_ADDRESS = "db.address";
	public static String DATABASE_NAME = "db.name";
	public static String DATABASE_USER = "db.user";
	public static String DATABASE_USER_DB = "db.user.db";
	public static String DATABASE_USER_PASSWORD = "db.user.password";
	public static String AUTORESPONSE_DURATION = "handler.autoresponse.duration";
	public static String WEBSITE = "website";
	
	public static String LOGGING_FILE_LOCATION = "logging.dir";
	
	private final Properties props;
	
	Configuration(final Properties props) {
		this.props = props;
	}
	
	public String getConfig(String key) {
		return props.getProperty(key);
	}

}
