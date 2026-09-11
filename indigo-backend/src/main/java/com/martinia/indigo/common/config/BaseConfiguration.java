package com.martinia.indigo.common.config;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class BaseConfiguration {
	@org.springframework.beans.factory.annotation.Autowired
	private org.springframework.beans.factory.ObjectProvider<com.martinia.indigo.metadata.application.reviews.ReviewProviderRequestPolicy> reviewPolicy;
	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5_000);
		factory.setReadTimeout(10_000);
		return new RestTemplate(factory);
	}

	@Bean
	public JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl();
	}

	@Bean
	public WebClient webClient() {
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setDownloadImages(false);
		webClient.getOptions().setTimeout(10_000);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
		webClient.setWebConnection(new com.martinia.indigo.metadata.application.reviews.ThrottledReviewConnection(webClient.getWebConnection(), () -> reviewPolicy.getObject()));
		return webClient;
	}
}
