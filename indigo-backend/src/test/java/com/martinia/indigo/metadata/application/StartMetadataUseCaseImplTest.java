package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.amazon.FindAmazonReviewsUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsAuthorUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsBookUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsReviewsUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

public class StartMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private DataUtils dataUtils;

	@MockBean
	private FindGoodReadsBookUseCase findGoodReadsBookUseCase;

	@MockBean
	private FindGoodReadsAuthorUseCase findGoodReadsAuthorUseCase;

	@MockBean
	private FindGoodReadsReviewsUseCase findGoodReadsReviewsUseCase;

	@MockBean
	private FindWikipediaAuthorUseCase findWikipediaAuthorUseCase;

	@MockBean
	private FindGoogleBooksBookUseCase findGoogleBooksBookUseCase;

	@MockBean
	private FindAmazonReviewsUseCase findAmazonReviewsUseCase;
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
				.thenReturn(Optional.of(ConfigurationMongoEntity.builder().key("goodreads.key").value("123456").build()));

		// When
		startMetadataUseCase.start(lang, type, entity);

		// Then
		Mockito.verify(mockConfigurationRepository, Mockito.atLeast(1)).findByKey("goodreads.key");
		Mockito.verify(mockConfigurationRepository, Mockito.atLeast(1)).findByKey("metadata.pull");

	}

}
