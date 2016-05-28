package com.cheryl.resources;

import java.util.Map;

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

import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cheryl.backend.ElasticClient;
import com.cheryl.bean.SearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

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
         ) throws JsonProcessingException{
      /*
       * TO DO: call backend api get result. Convert result to json
       */
      SearchResults sr = elasticClient.searchByRange(queryItem,Integer.parseInt(from), Integer.parseInt(to));
      /*
       * sr to json,json to string to queryItem + from + ":" +to+"\n"
       * 
       */

      SearchHit[] results = sr.getElasticResponse().getHits().getHits();
      for (SearchHit hit : results) {
         Map<String,Object> result = hit.getSource();      
      }
      
      /*
      // instance a json mapper
      ObjectMapper mapper = new ObjectMapper(); // create once, reuse

      // generate json
      byte[] json = mapper.writeValueAsBytes(yourinstancebean);
      // https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-index.html
      */
      
//    JsonObject value = Json.createObjectBuilder().add("query", queryItem + from + to).build();
//    resumeWithResponse(ar, value.toString());     ???not sure
      
      
      resumeWithResponse(ar, queryItem + from + ":" +to+"\n");
   }

   private static void resumeWithResponse(AsyncResponse ar, String res){
      Response response = Response.ok(res).type("application/json; charset=utf-8").header("content-length", res.length()).build();
      ar.resume(response);
   }
}
