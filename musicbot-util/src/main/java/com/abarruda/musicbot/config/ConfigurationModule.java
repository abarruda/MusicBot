package com.abarruda.musicbot.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ConfigurationModule extends AbstractModule {
	
private final String configFileLocation;
	
	public ConfigurationModule(final String configFileLocation) {
		this.configFileLocation = configFileLocation;
	}

	@Override
	protected void configure() {
		bindConstant().annotatedWith(ConfigurationProvider.PropsFileLoc.class).to(this.configFileLocation);
		bind(Configuration.class).toProvider(ConfigurationProvider.class).in(Scopes.SINGLETON);
	}

}
