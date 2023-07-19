package com.martinia.indigo.config;

import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BeanDefinitionConfigTest {

	@Bean
	@Primary
	public ApplicationEventPublisher applicationEventPublisherSpy(ApplicationEventPublisher applicationEventPublisher) {
		return Mockito.spy(applicationEventPublisher);
	}

}
