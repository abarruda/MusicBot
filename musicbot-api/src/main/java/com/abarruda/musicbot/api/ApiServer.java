package com.abarruda.musicbot.api;

import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.config.Configuration;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import io.github.mikexliu.stack.server.StackServer;


public class ApiServer {
	
	public interface Factory {
		public ApiServer create(final ApiModule apiModule);
	}
	
	private static final Logger logger = LogManager.getLogger(ApiServer.class);
	
	private StackServer server;
	
	@Inject
	private ApiServer(
			final Configuration configuration,
			@Assisted ApiModule apiModule) {
		
		int port = Integer.valueOf(configuration.getConfig(Configuration.API_PORT));
		server = null;
		
		try {
			server = StackServer.builder()
				.withTitle("MusicBot API")
				.withDescription("server-example description")
				.withApiPackageName("com.abarruda.musicbot.api")
	            .withVersion("0.0.1-SNAPSHOT")
	            .withSwaggerUiDirectory("swagger-ui")
	            .withSwaggerEnabled()
	            .withExceptionHandler(throwable ->
	                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                            .build())
	            .withPort(port)
	            .withCorsEnabled()
	            .withModule(apiModule)
				.build();
		} catch (Exception e) {
			logger.fatal("Cannot start stack server!", e);
			System.exit(1);
		}	
	}
	
	public void start() throws Exception {
		if (server != null) {
			this.server.start();
		}
	}
	
	public static void main(String[] args) {
		final ApiModule apiModule = new ApiModule(args[0]);
		final Injector injector = Guice.createInjector(apiModule);
		final ApiServer.Factory factory = injector.getInstance(ApiServer.Factory.class);
		try {
			factory.create(apiModule).start();
		} catch (Exception e) {
			logger.fatal("Cannot start API server!", e);
		}
	}

}
