package com.cheryl.backend;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ElasticClientTest
{

   @Test
   public void test() throws JsonProcessingException
   {
      ElasticClient test = new ElasticClient();
      String query = "Harry Potter and the Cursed Child";
      test.searchByRange("Harry Potter and the Cursed Child", 0, 0);
   }

}
