package com.martinia.indigo.common.configuration.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.application.SaveConfigurationsUseCaseImpl;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SaveConfigurationsUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private ConfigurationRepository configurationRepository;

	@Resource
	private SaveConfigurationsUseCaseImpl useCase;

	@Test
	public void testSave_When_ConfigurationsExistAndModified_Then_SaveModifiedConfiguration() {
		// Given
		Configuration configuration1 = new Configuration();
		configuration1.setKey("key1");
		configuration1.setValue("value1");

		Configuration configuration2 = new Configuration();
		configuration2.setKey("key2");
		configuration2.setValue("value2");

		List<Configuration> configurations = Arrays.asList(configuration1, configuration2);

		ConfigurationMongoEntity existingConfiguration = new ConfigurationMongoEntity();
		existingConfiguration.setKey("key1");
		existingConfiguration.setValue("oldValue");

		when(configurationRepository.findByKey("key1")).thenReturn(Optional.of(existingConfiguration));
		when(configurationRepository.findByKey("key2")).thenReturn(Optional.empty());

		// When
		useCase.save(configurations);

		// Then
		verify(configurationRepository, times(1)).save(existingConfiguration);
		assertEquals("value1", existingConfiguration.getValue());
	}

	@Test
	public void testSave_When_ConfigurationsDoNotExist_Then_SaveAllConfigurations() {
		// Given
		Configuration configuration1 = new Configuration();
		configuration1.setKey("key1");
		configuration1.setValue("value1");

		Configuration configuration2 = new Configuration();
		configuration2.setKey("key2");
		configuration2.setValue("value2");

		List<Configuration> configurations = Arrays.asList(configuration1, configuration2);

		when(configurationRepository.findByKey(anyString())).thenReturn(Optional.empty());

		// When
		useCase.save(configurations);

		// Then
		verify(configurationRepository, times(1)).save(any());
		verify(configurationRepository, times(1)).save(any());
	}
}

