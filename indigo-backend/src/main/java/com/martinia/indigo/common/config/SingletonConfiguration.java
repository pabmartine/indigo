package com.martinia.indigo.common.config;

import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.martinia.indigo.common.singletons.MetadataSingleton;

@Configuration
public class SingletonConfiguration {

	@Bean
	public MetadataSingleton metadataSingleton() {
		return new MetadataSingleton();
	}

	@Bean
	public UploadEpubFilesSingleton uploadEpubFilesSingleton() {
		return new UploadEpubFilesSingleton();
	}
}