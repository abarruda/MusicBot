package com.abarruda.musicbot.api.users.v1.resource;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.abarruda.musicbot.items.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Users API")
@Path("/api/musicbot/users/v1")
public abstract class UsersResource {
	
	@ApiOperation(value = "Get users", notes = "Get users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public abstract Set<User> getUsersByChatId(
    		@PathParam(value = "id")
    		final String chatId);	

}
