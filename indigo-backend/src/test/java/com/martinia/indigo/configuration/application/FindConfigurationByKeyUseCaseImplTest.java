package com.martinia.indigo.configuration.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.domain.ports.usecases.FindConfigurationByKeyUseCase;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.configuration.infrastructure.mongo.mappers.ConfigurationMongoMapper;

public class FindConfigurationByKeyUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindConfigurationByKeyUseCase findConfigurationByKeyUseCase;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@MockBean
	private ConfigurationMongoMapper configurationMongoMapper;

	@Test
	public void testFindByKey_ReturnsConfigurationByKey() {
		// Given
		String key = "configuration_key";

		ConfigurationMongoEntity configurationEntity = new ConfigurationMongoEntity();
		configurationEntity.setKey(key);

		Configuration configuration = new Configuration();
		configuration.setKey(key);

		when(configurationRepository.findByKey(key)).thenReturn(Optional.of(configurationEntity));
		when(configurationMongoMapper.entity2Domain(configurationEntity)).thenReturn(configuration);

		// When
		Optional<Configuration> result = findConfigurationByKeyUseCase.findByKey(key);

		// Then
		assertEquals(Optional.of(configuration), result);
	}

	@Test
	public void testFindByKey_ReturnsEmptyOptionalWhenConfigurationNotFound() {
		// Given
		String key = "non_existing_key";

		when(configurationRepository.findByKey(key)).thenReturn(Optional.empty());

		// When
		Optional<Configuration> result = findConfigurationByKeyUseCase.findByKey(key);

		// Then
		assertEquals(Optional.empty(), result);
	}
}
