package com.martinia.indigo.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.martinia.indigo.domain.singletons.MetadataSingleton;

@Configuration
public class SingletonConfiguration {

	@Bean
	public MetadataSingleton authorsSingleton() {
		return new MetadataSingleton();
	}
}