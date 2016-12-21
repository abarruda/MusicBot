package com.abarruda.musicbot.api;

import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.config.Config;

import io.github.mikexliu.stack.server.StackServer;


public class ApiServer {
	
	private static final Logger logger = LogManager.getLogger(ApiServer.class);
	
	private StackServer server;
	
	private ApiServer() {
		
		int port = Integer.valueOf(Config.getConfig(Config.API_PORT));
		server = null;
		
		try {
			server = StackServer.builder()
				.withTitle("MusicBoy API")
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
				.build();
		} catch (Exception e) {
			logger.fatal("Cannot start stack server!");
			System.exit(1);
		}
		
	}
	
	public void start() throws Exception {
		if (server != null) {
			this.server.start();
		}
	}
	
	public static void main(String[] args) {
		Config.initializeConfigs(args[0]);
		
		final ApiServer server = new ApiServer();
		try {
			server.start();
		} catch (Exception e) {
			logger.fatal("Cannot start API server!", e);
		}
		
	}

}
