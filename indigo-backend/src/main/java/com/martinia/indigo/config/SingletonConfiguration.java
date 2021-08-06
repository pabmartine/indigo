package com.martinia.indigo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.martinia.indigo.singletons.MetadataSingleton;

@Configuration
public class SingletonConfiguration {

	@Bean
	public MetadataSingleton authorsSingleton() {
		return new MetadataSingleton();
	}
}