package com.martinia.indigo.metadata.application;

import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(MetadataAutostartProperties.class)
public class MetadataAutostart {

	private final MetadataAutostartProperties properties;
	private final StartMetadataUseCase startMetadataUseCase;

	@EventListener(ApplicationReadyEvent.class)
	public void start() {
		if (!properties.isEnabled()) {
			return;
		}
		MetadataAutostartProperties.Type type = properties.getType();
		try {
			log.info("Automatically starting metadata process {} (language: {})", type, properties.getLang());
			startMetadataUseCase.start(properties.getLang(), type.getProcessType(), type.getEntity());
		}
		catch (RuntimeException exception) {
			// An existing durable review job must not be replaced or prevent application startup.
			log.warn("Could not automatically start metadata process {}", type, exception);
		}
	}
}
