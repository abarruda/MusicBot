package com.abarruda.musicbot.api.sets.v1.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "Sets Api", description = "")
@Path("/api/sets/v1")
public abstract class SetsResource {
	
	@ApiOperation(value = "test", notes = "get set")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/test")
    public abstract String test();


}
