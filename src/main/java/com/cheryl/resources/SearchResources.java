package com.cheryl.resources;

import javax.naming.directory.SearchResult;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cheryl.backend.ElasticClient;
import com.cheryl.bean.SearchResults;
@Component
@Path("/")
public class SearchResources {
	
    @Autowired
    private ElasticClient elasticClient;
    
    @GET
    @Path("search")
    public void get(@Suspended final AsyncResponse ar,
                    @Context final HttpHeaders headers,
                    @QueryParam("query") @DefaultValue("") final String queryItem,
                    @QueryParam("from") @DefaultValue("") final String from,
                    @QueryParam("to") @DefaultValue("") final String to
                    ){
    	/*
    	 * TO DO: call backend api get result. Convert result to json
    	 */
    	SearchResults sr = elasticClient.searchByRange(queryItem,Integer.parseInt(from), Integer.parseInt(to));
        /*
         * sr to json,json to string to queryItem + from + ":" +to+"\n"
         * 
         */
    	resumeWithResponse(ar, queryItem + from + ":" +to+"\n");
    }
    
    private static void resumeWithResponse(AsyncResponse ar, String res){
        Response response = Response.ok(res).type("application/json; charset=utf-8").header("content-length", res.length()).build();
        ar.resume(response);
    }
}
