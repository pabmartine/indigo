package com.martinia.indigo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BaseConfiguration {
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
