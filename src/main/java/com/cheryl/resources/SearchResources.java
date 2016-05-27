package com.cheryl.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

@Component
@Path("/")
public class SearchResources {
    
    @GET
    @Path("search")
    public void get(@Suspended final AsyncResponse ar,
                    @Context final HttpHeaders headers,
                    @QueryParam("query") @DefaultValue("") final String queryItem,
                    @QueryParam("from") @DefaultValue("") final String from,
                    @QueryParam("to") @DefaultValue("") final String to
                    ){
        resumeWithResponse(ar, queryItem + from + ":" +to+"\n");
    }
    
    private static void resumeWithResponse(AsyncResponse ar, String res){
        Response response = Response.ok(res).type("application/json; charset=utf-8").header("content-length", res.length()).build();
        ar.resume(response);
    }
}
