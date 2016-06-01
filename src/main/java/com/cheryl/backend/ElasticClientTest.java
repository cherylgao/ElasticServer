package com.cheryl.backend;

import static org.junit.Assert.*;

import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ElasticClientTest {
   @Test
   public void test() throws JsonProcessingException {
      ElasticClient test = new ElasticClient();
      String query = "You Are a Badass: How to Stop Doubting Your Greatness and Start Living an Awesome Life";
      //test.searchByRange(query, 0, 100);
      test.searchByRange("Textbooks", "", 0, 100);
      //test.searchByRange("Textbooks", "", "", "", "", 0, 100);
      //
   }
}
