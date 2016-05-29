package com.cheryl.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MultiMatchQuery.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.cheryl.bean.SearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;

import org.elasticsearch.common.text.Text; 

public class ElasticClient {
   private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
   
	private Client client;
	public ElasticClient(){
		try {
			client = TransportClient
					.builder()
					.build()
					.addTransportAddress(
							new InetSocketTransportAddress(InetAddress
									.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		client.admin().indices().prepareCreate("twitter5")   
      .addMapping("tweet", "{\n" +                
              "    \"tweet\": {\n" +
              "      \"properties\": {\n" +
              "        \"message\": {\n" +
              "          \"type\": \"string\"\n" +
              "        }\n" +
              "      }\n" +
              "    }\n" +
              "  }")
      .get();
      */
	}
	public SearchResults searchByRange(String query, int start, int end) {
		// TODO call function;km n, 
	   
	   // for basic interface	   
	   //MatchQueryBuilder qb = matchQuery("title", query); 
	   MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery(
	         query,     // Text you are looking for
	         "title", "description", "url", "author", "ISBN1", "ISBN2", "tag" // Fields you query on
	         );
	   
	     //fuzzyQuery   
	   System.out.println("start: " + start);
	   int size = end - start + 1;
	   System.out.println("size: " + size);
	   
	   SearchResponse elasticResponse = client.prepareSearch("books")
            .setTypes("book")
            .setSearchType(SearchType.QUERY_THEN_FETCH)
            .setQuery(qb)
            .setFrom(start)
            .setSize(size)
            .addSort("price", SortOrder.ASC) // sort by price
            .addHighlightedField("title") //snippet
            .addHighlightedField("description")
            .execute()
            .actionGet();
	   
	   SearchHit[] results = elasticResponse.getHits().getHits();
      System.out.println("Current results: " + results.length);
      
      StringBuilder arrayJson = new StringBuilder();
      arrayJson.append("[");
      
      for (SearchHit hit : results) {
          System.out.println("------------------------------");
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
      System.out.println(arrayFinalJson);
      	  
		return new SearchResults(elasticResponse);
	}
}
