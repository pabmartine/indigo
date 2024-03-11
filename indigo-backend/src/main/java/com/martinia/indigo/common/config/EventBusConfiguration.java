package com.martinia.indigo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EventBusConfiguration {

  @Bean(name = "applicationEventMulticaster")
  public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
    SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
    threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() + 2);
    threadPoolTaskExecutor.initialize();
    eventMulticaster.setTaskExecutor(threadPoolTaskExecutor);
    return eventMulticaster;
  }
}