package com.abarruda.musicbot.api.sets.v1.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.abarruda.musicbot.items.MusicSet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "Sets API")
@Path("/api/sets/v1")
public abstract class SetsResource {
	
	@ApiOperation(value = "Get sets", notes = "Get Sets")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public abstract Iterable<MusicSet> getSetsByChatId(
    		@PathParam(value = "id")
    		final String id, 
    		
    		@ApiParam(value = "User ID to filter by", required = false)
    		@QueryParam(value = "userId")
    		final String user,
    		
    		@ApiParam(value = "", required = false)
    		@QueryParam(value = "orderByReferenceCount")
    		final boolean orderByReferenceCount);
	
	@ApiOperation(value = "Get recent music content")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/recent")
	public abstract Iterable<MusicSet> getRecentSetsByChatId(
			@PathParam(value = "id")
			final String id,
			
			@ApiParam(value = "Duration to retrieve music content up to.", required = true)
			@QueryParam(value = "duration")
			final String durationString);


}
