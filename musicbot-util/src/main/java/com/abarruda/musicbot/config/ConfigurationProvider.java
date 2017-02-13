package com.abarruda.musicbot.config;

import java.io.FileInputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ConfigurationProvider implements Provider<Configuration> {
	
	private static final Logger logger = LogManager.getLogger(Configuration.class);
	
	private final String propsFileLocation;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER})
	@BindingAnnotation
	public @interface PropsFileLoc {}
	
	@Inject
	public ConfigurationProvider(@PropsFileLoc String propsFileLocation) {
		this.propsFileLocation = propsFileLocation;
	}

	@Override
	public Configuration get() {
		final Properties props = new Properties();
		try {
			FileInputStream input = new FileInputStream(propsFileLocation); 
			props.load(input);
			input.close();
		} catch (Exception e) {
			logger.fatal("Cannot initialize configurations!", e);
			System.exit(1);
		}
		return new Configuration(props);
	}

}
