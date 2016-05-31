package com.cheryl.backend;

import static org.junit.Assert.*;

import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ElasticClientTest {
   @Test
   public void test() throws JsonProcessingException {
      ElasticClient test = new ElasticClient();
      String query = "Alexander Hamilto";
      test.searchByRange(query, 0, 100);
      //test.searchByRange("", query, 0, 100);
      //test.searchByRange("", "9780545162074", "", "", "", 0, 100);
   }
}
