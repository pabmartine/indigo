package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.metadata.domain.service.StartMetadataUseCase;
import com.martinia.indigo.ports.out.metadata.AmazonService;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;
import com.martinia.indigo.ports.out.metadata.GoogleBooksService;
import com.martinia.indigo.ports.out.metadata.WikipediaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

public class StartMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private DataUtils dataUtils;

	@MockBean
	private GoodReadsService goodReadsService;

	@MockBean
	private WikipediaService wikipediaService;

	@MockBean
	private GoogleBooksService googleBooksService;

	@MockBean
	private AmazonService amazonService;
	@MockBean
	private ConfigurationRepository mockConfigurationRepository;

	@Resource
	private StartMetadataUseCase startMetadataUseCase;

	@Test
	public void testStart_WhenTypeIsFullAndEntityIsAll_ThenInvokeInitialLoad() {
		// Given
		String lang = "en";
		String type = "none";
		String entity = "all";

		Mockito.when(mockConfigurationRepository.findByKey("goodreads.key"))
				.thenReturn(Optional.of(new Configuration("goodreads.key", "123456")));
		Mockito.when(mockConfigurationRepository.findByKey("metadata.pull"))
				.thenReturn(Optional.of(new Configuration("metadata.pull", "1000")));
		Mockito.when(mockConfigurationRepository.findByKey("metadata.pull"))
				.thenReturn(Optional.of(new Configuration("metadata.pull", "1000")));

		// When
		startMetadataUseCase.start(lang, type, entity);

		// Then
		Mockito.verify(mockConfigurationRepository, Mockito.atLeast(1)).findByKey("goodreads.key");
		Mockito.verify(mockConfigurationRepository, Mockito.atLeast(1)).findByKey("metadata.pull");

	}

}
