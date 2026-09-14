package com.martinia.indigo.common.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EventBusConfiguration {

  @Bean(name = "eventTaskExecutor")
  public ThreadPoolTaskExecutor eventTaskExecutor(
      @Value("${events.workers:1}") int workers,
      @Value("${events.queue-capacity:32}") int queueCapacity) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    int size = Math.max(1, Math.min(8, workers));
    executor.setCorePoolSize(size);
    executor.setMaxPoolSize(size);
    executor.setQueueCapacity(Math.max(1, queueCapacity));
    executor.setThreadNamePrefix("indigo-event-");
    // Slow producers down when listeners cannot keep up, without dropping events.
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    return executor;
  }

  @Bean(name = "applicationEventMulticaster")
  public ApplicationEventMulticaster simpleApplicationEventMulticaster(
      @Qualifier("eventTaskExecutor") ThreadPoolTaskExecutor executor) {
    SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
    multicaster.setTaskExecutor(executor);
    return multicaster;
  }
}
