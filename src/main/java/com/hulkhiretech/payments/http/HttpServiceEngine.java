package com.hulkhiretech.payments.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor

public class HttpServiceEngine {
	
	private final RestClient restClient;
	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {
		log.info("Http call making..");
		try {
		ResponseEntity<String> response=restClient.method(httpRequest.getHttpMethod())
		.uri(httpRequest.getUrl())
		.contentType(MediaType.APPLICATION_JSON)
		.body(httpRequest.getBody())
		.retrieve().toEntity(String.class);
		
		log.info("response from api is :{}",response);
		return response;
		}
		catch(Exception e) {
			log.error("Got exception while calling api");
			throw new RuntimeException("failed while calling api", e);
		}
		
	}

}
