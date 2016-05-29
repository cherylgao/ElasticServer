package com.cheryl.backend;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ElasticClientTest
{

   @Test
   public void test() {
      ElasticClient test = new ElasticClient();
      String query = "Machine Learning";
      //test.searchByRange(query, 0, 100);
      test.searchByRange("", "Harry", "1617290181", "Peter Harrington", 0, 10000);
   }
}
