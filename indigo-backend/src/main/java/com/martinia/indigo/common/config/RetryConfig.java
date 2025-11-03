package com.martinia.indigo.common.config;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@NoArgsConstructor
public class RetryConfig {}