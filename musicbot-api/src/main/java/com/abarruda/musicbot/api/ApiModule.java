package com.abarruda.musicbot.api;

import com.abarruda.musicbot.config.ConfigurationModule;
import com.abarruda.musicbot.persistence.DatabaseModule;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiModule extends AbstractModule {
	
	private final String propertyFileLocation;
	
	public ApiModule(final String propertFileLocation) {
		this.propertyFileLocation = propertFileLocation;
	}

	@Override
	protected void configure() {
		install(new ConfigurationModule(this.propertyFileLocation));
		install(new DatabaseModule());
		
		install(new FactoryModuleBuilder()
				.implement(ApiServer.class, ApiServer.class)
				.build(ApiServer.Factory.class));
	}

}
