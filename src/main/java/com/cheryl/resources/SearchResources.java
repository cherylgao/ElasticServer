package com.cheryl.resources;

import java.util.ArrayList;
import java.util.HashMap;
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

import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cheryl.backend.ElasticClient;
import com.cheryl.bean.SearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
@Path("/")
public class SearchResources {

   @Autowired
   private ElasticClient elasticClient;
   private SearchResults sr;
   private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
   

   @GET
   @Path("search")
   public void get(@Suspended final AsyncResponse ar,
         @Context final HttpHeaders headers,
         @QueryParam("query") @DefaultValue("") final String queryItem,
         @QueryParam("from") @DefaultValue("") final String from,
         @QueryParam("to") @DefaultValue("") final String to,
         @QueryParam("genre") @DefaultValue("") final String queryGenre,
         @QueryParam("title") @DefaultValue("") final String queryTitle,
         @QueryParam("isbn") @DefaultValue("") final String queryISBN,
         @QueryParam("author") @DefaultValue("") final String queryAuthor,
         @QueryParam("minPrice") @DefaultValue("") final String queryminPrice,
         @QueryParam("maxPrice") @DefaultValue("") final String querymaxPrice,
         @QueryParam("adv") @DefaultValue("") final String adv
         ) throws JsonProcessingException{
      /*
       * TO DO: call backend api get result. Convert result to json
       */
      if (adv.length() == 0) {
         sr = elasticClient.searchByRange(queryItem, Integer.parseInt(from), Integer.parseInt(to));
      } else {
         sr = elasticClient.searchByRange(queryGenre, queryTitle, queryISBN, queryAuthor, queryminPrice, querymaxPrice);
      }
            
      /*
       * sr to json,json to string to queryItem + from + ":" +to+"\n" 
       */

      if (sr == null) {
         System.out.println("null");
      }
      
      SearchHit[] results = sr.getElasticResponse().getHits().getHits();
      StringBuilder arrayJson = new StringBuilder();
      arrayJson.append("[");
      
      for (SearchHit hit : results) {
          Map<String,Object> result = hit.getSource();
          StringBuilder excerptBuilder = new StringBuilder();
          for (Map.Entry<String, HighlightField> highlight : hit.getHighlightFields().entrySet()) { 
              for (Text text : highlight.getValue().fragments()) { 
                  excerptBuilder.append(text.string()); 
                  excerptBuilder.append(" ... "); 
              } 
          } 

          String resultJson = gson.toJson(result);
          String resultWithSinppet = resultJson.substring(0, resultJson.length() - 1) 
                + ",\"snippet\":\"" + excerptBuilder.toString() + "\"}";           
          arrayJson.append(resultWithSinppet);
          arrayJson.append(",");
      }
      
      String arrayFinalJson = arrayJson.substring(0, arrayJson.length() - 1) + "]";      
      System.out.println("in returnFrontEnd: " + arrayFinalJson);
      
//    JsonObject value = Json.createObjectBuilder().add("query", queryItem + from + to).build();
//    resumeWithResponse(ar, value.toString());     ???not sure
      
      resumeWithResponse(ar, arrayFinalJson);
   }

   private static void resumeWithResponse(AsyncResponse ar, String res){
      Response response = Response.ok(res).type("application/json; charset=utf-8").header("content-length", res.length()).header("Access-Control-Allow-Origin", "*").build();
      ar.resume(response);
   }
}
