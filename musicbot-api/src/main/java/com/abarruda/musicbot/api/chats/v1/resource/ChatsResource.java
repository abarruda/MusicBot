package com.abarruda.musicbot.api.chats.v1.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.abarruda.musicbot.items.Chat;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Chats API")
@Path("/api/musicbot/chats/v1")
public abstract class ChatsResource {
	
	@ApiOperation(value = "Get chat", notes = "Get Chats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
	public abstract Chat getChat(
			@PathParam(value = "id")
			final String id);

}
