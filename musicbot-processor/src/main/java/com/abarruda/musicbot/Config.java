package com.abarruda.musicbot;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Config {
	private static final Logger logger = LogManager.getLogger(Config.class);
	
	private static Properties props = new Properties();
	
	public static String BOT_NAME = "bot.name";
	public static String API_TOKEN = "api.token";
	public static String DATABASE_ADDRESS = "db.address";
	public static String DATABASE_NAME = "db.name";
	public static String AUTORESPONSE_DURATION = "handler.autoresponse.duration";
	
	public static void initializeConfigs(String propertyFile) {
		try {
			FileInputStream input = new FileInputStream(propertyFile); 
			props.load(input);
			input.close();
		} catch (Exception e) {
			logger.fatal("Cannot start bot!  Configuration error!", e);
			System.exit(1);
		}	
	}
	
	public static String getConfig(String key) {
		return props.getProperty(key);
	}

}
