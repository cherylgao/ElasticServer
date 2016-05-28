package com.cheryl.bean;

import org.elasticsearch.action.search.SearchResponse;

public class SearchResults {
	private SearchResponse elasticResponse;
	
	public SearchResults(SearchResponse elasticResponse) {
	   this.elasticResponse = elasticResponse;
	}
	
   public SearchResponse getElasticResponse() {
      return elasticResponse;
   }
   
   public void setElasticResponse(SearchResponse elasticResponse) {
      this.elasticResponse = elasticResponse;
   }
	
}
