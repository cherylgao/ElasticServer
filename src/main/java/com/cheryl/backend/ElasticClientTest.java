package com.cheryl.backend;

import static org.junit.Assert.*;

import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ElasticClientTest
{

   @Test
   public void test() {
      ElasticClient test = new ElasticClient();
      String query = "Alexander Hamilton";
      test.searchByRange(query, 0, 100);
      // test.searchByRange("", "", "James Alexander", "", "", "");
   }
}
