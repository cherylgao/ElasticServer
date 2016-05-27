package com.cheryl.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.cheryl.bean.SearchResults;

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
	}
	public SearchResults searchByRange(String query, int start, int end){
		// TODO call function;km n, 
		return null;
	}
}
