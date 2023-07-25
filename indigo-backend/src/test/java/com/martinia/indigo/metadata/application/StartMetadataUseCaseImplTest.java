package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartMetadataUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private StartMetadataUseCase startMetadataUseCase;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@Mock
	private MetadataSingleton metadataSingleton;

	@Test
	@Disabled
	public void testStartMetadata_StartProcess_MetadataSingletonStarted() {
		// Given
		String lang = "en";
		String type = "none";
		String entity = "all";

		when(configurationRepository.findByKey("goodreads.key")).thenReturn(
				Optional.ofNullable(ConfigurationMongoEntity.builder().key("goodreads.key").value("123456").build()));
		when(metadataSingleton.isRunning()).thenReturn(false);

		// When
		startMetadataUseCase.start(lang, type, entity);

		// Then
		verify(metadataSingleton).start(anyString(), anyString());
	}



}
