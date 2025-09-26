package com.martinia.indigo.config;

import com.gargoylesoftware.htmlunit.WebClient;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class BeanDefinitionConfigTest {

	@Bean
	@Primary
	public ApplicationEventPublisher applicationEventPublisherSpy(ApplicationEventPublisher applicationEventPublisher) {
		return Mockito.spy(applicationEventPublisher);
	}

	@Bean
	@Primary
	public JavaMailSender javaMailSenderMock() {
		return Mockito.mock(JavaMailSenderImpl.class);
	}

	@Bean
	@Primary
	public WebClient webClientMock() {
		return Mockito.mock(WebClient.class);
	}


}
