package com.cheryl.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.search.MultiMatchQuery.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.cheryl.bean.SearchResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;

import org.elasticsearch.common.text.Text; 
import org.elasticsearch.common.unit.Fuzziness;
import org.json.JSONArray;

public class ElasticClient {

   private Client client;
   private SearchResponse elasticResponse;;

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

      //client.admin().indices().prepareRefresh().execute().actionGet();		
   }

   // for basic interface  
   public SearchResults searchByRange(String query, int start, int end) throws JsonProcessingException {

      int size = end - start + 1;

      // if query is empty, return all books; otherwise, return match query      
      if (query == null || query.length() == 0) {
         MatchAllQueryBuilder qbAll = QueryBuilders.matchAllQuery();
         elasticResponse = client.prepareSearch("books")
               .setTypes("book")
               .setSearchType(SearchType.QUERY_THEN_FETCH)
               .setQuery(qbAll)            
               .setFrom(start)
               .setSize(size)
               .execute()
               .actionGet();
      } else {
         MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery(
               query,     // Text you are looking for
               "title", "description", "url", "author", "ISBN10", "ISBN13", "tag" // Fields you query on
               ).fuzziness(Fuzziness.AUTO);

         elasticResponse = client.prepareSearch("books")
               .setTypes("book")
               .setSearchType(SearchType.QUERY_THEN_FETCH)
               .setQuery(qb)            
               .setFrom(start)
               .setSize(size)
               .addHighlightedField("title").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>") //snippet
               .addHighlightedField("description").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("author").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN10").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN13").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .execute()
               .actionGet();
      }

      SearchHit[] results = elasticResponse.getHits().getHits();
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
         list.add(result);
      }

      ObjectMapper mapper = new ObjectMapper();
      String val = mapper.writeValueAsString(list);

      String resultJsonString = val.trim();
      System.out.println("ElasticClient, Basic Search; Current Result: " + results.length); 
      System.out.println(resultJsonString);

      return new SearchResults(elasticResponse);
   }


   // for basic interface2
   // if both queryGenre and query are empty, return all books
   // if queryGenre is empty, return match of query
   // if query is empty, return term match of genre
   // otherwise, return match both query and genre
   public SearchResults searchByRange(String queryGenre, String query, int start, int end) throws JsonProcessingException {
      int size = end - start + 1;

      if ((queryGenre == null || queryGenre.length() == 0) && (query == null || query.length() == 0)) {
         MatchAllQueryBuilder qbAll = QueryBuilders.matchAllQuery();
         elasticResponse = client.prepareSearch("books")
               .setTypes("book")
               .setSearchType(SearchType.QUERY_THEN_FETCH)
               .setQuery(qbAll)            
               .setFrom(start)
               .setSize(size)
               .addHighlightedField("title").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>") //snippet
               .addHighlightedField("description").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("author").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN10").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN13").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .execute()
               .actionGet();
      } else if (queryGenre == null || queryGenre.length() == 0) {
         MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery(
               query,     // Text you are looking for
               "title", "description", "url", "author", "ISBN10", "ISBN13", "tag" // Fields you query on
               ).fuzziness(Fuzziness.AUTO);
         elasticResponse = client.prepareSearch("books")
               .setTypes("book")
               .setSearchType(SearchType.QUERY_THEN_FETCH)
               .setQuery(qb)            
               .setFrom(start)
               .setSize(size)
               .addHighlightedField("title").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>") //snippet
               .addHighlightedField("description").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("author").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN10").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN13").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .execute()
               .actionGet();
      } else if (query == null || query.length() == 0) {
         MatchQueryBuilder qb = QueryBuilders.matchPhraseQuery("genre", queryGenre);         
         elasticResponse = client.prepareSearch("books")
               .setTypes("book")
               .setSearchType(SearchType.QUERY_THEN_FETCH)
               .setQuery(qb)            
               .setFrom(start)
               .setSize(size)
               .addHighlightedField("genre").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .execute()
               .actionGet();
      } else {
         MatchQueryBuilder qb1 = QueryBuilders.matchPhraseQuery("genre", queryGenre);  
         MultiMatchQueryBuilder qb2 = QueryBuilders.multiMatchQuery(
               query,     // Text you are looking for
               "title", "description", "url", "author", "ISBN10", "ISBN13", "tag" // Fields you query on
               ).fuzziness(Fuzziness.AUTO);
         BoolQueryBuilder qb = QueryBuilders.boolQuery()
               .must(qb1)
               .should(qb2);
         elasticResponse = client.prepareSearch("books")
               .setTypes("book")
               .setSearchType(SearchType.QUERY_THEN_FETCH)
               .setQuery(qb)            
               .setFrom(start)
               .setSize(size)
               .addHighlightedField("genre").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("title").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>") //snippet
               .addHighlightedField("description").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("author").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN10").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .addHighlightedField("ISBN13").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
               .execute()
               .actionGet();
      }


      SearchHit[] results = elasticResponse.getHits().getHits();
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
         list.add(result);
      }

      ObjectMapper mapper = new ObjectMapper();
      String val = mapper.writeValueAsString(list);

      String resultJsonString = val.trim();
      System.out.println("ElasticClient, Basic Search 2; Current Result: " + results.length); 
      System.out.println(resultJsonString);
      return new SearchResults(elasticResponse);
   }


   //for advanced interface
   public SearchResults searchByRange(String queryTitle, 
         String queryISBN, String queryAuthor, String queryminPrice, 
         String querymaxPrice, int start, int end) throws JsonProcessingException {

      int size = end - start + 1;
      double minPriceDouble = 0.0;
      double maxPriceDouble = 10000000.0;

      if (!queryminPrice.isEmpty() && queryminPrice.length() != 0) {
         minPriceDouble = Double.parseDouble(queryminPrice);
      }

      if (!querymaxPrice.isEmpty() && querymaxPrice.length() != 0) {
         maxPriceDouble = Double.parseDouble(querymaxPrice);
      }

      MatchAllQueryBuilder qbAll = QueryBuilders.matchAllQuery();
      
      BoolQueryBuilder qbISBN = QueryBuilders.boolQuery()          
            .should(QueryBuilders.matchPhraseQuery("ISBN10", queryISBN))
            .should(QueryBuilders.matchPhraseQuery("ISBN13", queryISBN))
            .minimumNumberShouldMatch(1);
      
      System.out.println("queryTitle: " + queryTitle);
      
      MatchQueryBuilder qbTitle = QueryBuilders.matchPhraseQuery("title", queryTitle).fuzziness(Fuzziness.AUTO);

      BoolQueryBuilder qb = QueryBuilders.boolQuery()
            .must((queryTitle.isEmpty() || queryTitle.length() == 0) ? qbAll : qbTitle)
            .must((queryISBN.isEmpty() || queryISBN.length() == 0) ? qbAll : qbISBN)
            .must((queryAuthor.isEmpty() || queryAuthor.length() == 0) ? qbAll : QueryBuilders.matchQuery("author", queryAuthor).fuzziness(Fuzziness.AUTO))
            .must(QueryBuilders.rangeQuery("price")
                  .from(minPriceDouble)
                  .to(maxPriceDouble)
                  .includeLower(true)
                  .includeUpper(true)); 

      SearchResponse elasticResponse = client.prepareSearch("books")
            .setTypes("book")
            .setSearchType(SearchType.QUERY_THEN_FETCH)
            .setQuery(qb)
            .setFrom(start)
            .setSize(size)
            .addSort("price", SortOrder.ASC)
            .addHighlightedField("title").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>") //snippet
            .addHighlightedField("description").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
            .addHighlightedField("author").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
            .addHighlightedField("ISBN10").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
            .addHighlightedField("ISBN13").setHighlighterPreTags("<b>").setHighlighterPostTags("</b>")
            .execute()
            .actionGet();

      SearchHit[] results = elasticResponse.getHits().getHits();
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
         list.add(result);
      }

      ObjectMapper mapper = new ObjectMapper();
      String val = mapper.writeValueAsString(list);

      String resultJsonString = val.trim();
      System.out.println("ElasticClient, Basic Search; Current Result: " + results.length); 
      System.out.println(resultJsonString);
      return new SearchResults(elasticResponse);
   }

}

/*
 * System.out.println("------------------------------");
      System.out.println("Return to Front End, Current results: " + results.length);

      StringBuilder arrayJson = new StringBuilder();
      arrayJson.append("[");

      ArrayList<Map<String, Object>> test = new ArrayList<>();

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
          test.add(result);
      }

      String testFinalJson = gson.toJson(test);

      String arrayFinalJson = arrayJson.substring(0, arrayJson.length() - 1) + "]";      
      System.out.println(arrayFinalJson);

 */


/*
SearchResponse response = client().prepareSearch("idx").setTypes("type")
      .setQuery(matchAllQuery())
      .addAggregation(terms("keys").field("key").size(3).order(Terms.Order.count(false)))
      .execute().actionGet();

Terms  terms = response.getAggregations().get("keys");
Collection<Terms.Bucket> buckets = terms.getBuckets();
assertThat(buckets.size(), equalTo(3));
 */


/*
After submitting a document to an in-memory node, I need to refresh the index:

node.client().admin().indices().prepareRefresh().execute().actionGet();
calling refresh fixed the problem.
 */


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