package com.cheryl.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

public class ElasticClient {
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
	   
	   System.out.println(results);
	   
      System.out.println("Current results: " + results.length);
      for (SearchHit hit : results) {
          System.out.println("------------------------------");
          Map<String,Object> result = hit.getSource();   
          System.out.println(result);
      }
	   
      /*
      // instance a json mapper
      ObjectMapper mapper = new ObjectMapper(); // create once, reuse

      // generate json
      byte[] json = mapper.writeValueAsBytes(results);
      
      System.out.println(json.toString());
      */
	   
		return new SearchResults(elasticResponse);
	}
}
