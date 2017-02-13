package com.abarruda.musicbot.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DatabaseModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DatabaseFacade.class).to(MongoDbFacade.class).in(Scopes.SINGLETON);
	}

}
