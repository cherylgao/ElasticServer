package com.cheryl.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MultiMatchQuery.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.cheryl.bean.SearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.*;

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
	public SearchResults searchByRange(String query, int start, int end) throws JsonProcessingException{
		// TODO call function;km n, 
	   
	   // for basic interface
	   
	   MatchQueryBuilder qb = matchQuery("title", query);                  
	   
	   SearchResponse elasticResponse = client.prepareSearch("books")
            .setTypes("book")
            .setSearchType(SearchType.QUERY_AND_FETCH)
            .setQuery(qb)
            .execute()
            .actionGet();
	   
	   SearchHit[] results = elasticResponse.getHits().getHits();
      System.out.println("Current results: " + results.length);
      
      String resultJsons = "";
      
      Map<String, Map<String, Object>> final_map = new HashMap<>();
      for (SearchHit hit : results) {
          System.out.println("------------------------------");
          Map<String,Object> result = hit.getSource();
          String id = hit.getId();
          final_map.put(id, result);
          //System.out.println(result);
      }
      String final_results = gson.toJson(final_map);
	   System.out.println(final_results);
    
	   
		return new SearchResults(elasticResponse);
	}
}
