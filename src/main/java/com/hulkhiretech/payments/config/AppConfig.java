package com.hulkhiretech.payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class AppConfig {
	
	@Bean 
	RestClient restClientConfig() {
		return RestClient.create();
	}
	

}
