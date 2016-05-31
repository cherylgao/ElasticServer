package com.cheryl.resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import org.json.JSONObject;
import org.json.JSONArray;

@Component
@Path("/")
public class SearchResources {

   @Autowired
   private ElasticClient elasticClient;
   private SearchResults sr;
   //private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
  
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
         @QueryParam("adv") @DefaultValue("") final String adv,
         @QueryParam("base") @DefaultValue("") final String base
         ) throws JsonProcessingException{

      if ((adv.isEmpty() || adv.length() == 0) && (base.isEmpty() || base.length() == 0)) {
         sr = elasticClient.searchByRange(queryItem, Integer.parseInt(from), Integer.parseInt(to));
      } else if (adv.isEmpty() || adv.length() == 0){
         sr = elasticClient.searchByRange(queryGenre, queryItem, Integer.parseInt(from), Integer.parseInt(to));
      } else {
         sr = elasticClient.searchByRange(queryTitle, queryISBN, queryAuthor, queryminPrice, querymaxPrice, Integer.parseInt(from), Integer.parseInt(to));
      }
               
      if (sr == null) {
         System.out.println("null");
      }
      
      SearchHit[] results = sr.getElasticResponse().getHits().getHits();
      System.out.println("------------------------------");
      System.out.println("Return to Front End, Current results: " + results.length);
      
      /*
      StringBuilder arrayJson = new StringBuilder();
      arrayJson.append("[");
      */
      
     // ArrayList<Map<String, Object>> resTest = new ArrayList<>();
     // JSONArray array = new JSONArray();
      
   //   JsonObject value = Json.createObjectBuilder();
      List<Object> list = new ArrayList<>();
      for (SearchHit hit : results) {
          Map<String,Object> result = hit.getSource();
          StringBuilder excerptBuilder = new StringBuilder();
          for (Map.Entry<String, HighlightField> highlight : hit.getHighlightFields().entrySet()) { 
             for (Text text : highlight.getValue().fragments()) { 
                excerptBuilder.append(text.string()); 
                excerptBuilder.append(" ... "); 
             } 
          } 
          result.put("snippet", excerptBuilder.toString().trim());
         // resTest.add(result);
          //JSONObject object = new JSONObject(result); //Json.createObjectBuilder().build();
          //array.put(object);
          /*
          String resultJson = gson.toJson(result);
          String resultWithSinppet = resultJson.substring(0, resultJson.length() - 1) 
                + ",\"snippet\":\"" + excerptBuilder.toString() + "\"}";           
          arrayJson.append(resultWithSinppet);
          arrayJson.append(",");
          resTest.add(result);
          */
          
          list.add(result);
      }
      ObjectMapper mapper = new ObjectMapper();
      String val = mapper.writeValueAsString(list);
      //JSONObject object = new JSONObject();
      //object.append("response", val);
      String resultJsonString = val.trim();     
     // String arrayFinalJson = arrayJson.substring(0, arrayJson.length() - 1) + "]";      
     // String restTestJson = gson.toJson(resTest);
      
      System.out.println("Json: " + resultJsonString);
      
      // JsonObject value = Json.createObjectBuilder().add("query", queryItem + from + to).build();
//    resumeWithResponse(ar, value.toString());     ???not sure
      
      resumeWithResponse(ar, resultJsonString);
      
   }
   
   private static void resumeWithResponse(AsyncResponse ar, JSONObject res){
	      Response response = Response.ok(res).type("application/json; charset=utf-8").header("content-length", res.length()).header("Access-Control-Allow-Origin", "*").build();
	      ar.resume(response);
   }
   
   private static void resumeWithResponse(AsyncResponse ar, String res){
      Response response = Response.ok(res).type("application/json; charset=utf-8").header("content-length", res.getBytes(StandardCharsets.UTF_8).length).header("Access-Control-Allow-Origin", "*").build();
      ar.resume(response);
   }
}
