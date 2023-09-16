package com.martinia.indigo.common.configuration.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.configuration.application.SaveConfigurationsUseCaseImpl;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.configuration.infrastructure.mongo.mappers.ConfigurationMongoMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SaveConfigurationsUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private SaveConfigurationsUseCaseImpl saveConfigurationsUseCase;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@MockBean
	private ConfigurationMongoMapper configurationMongoMapper;

	@Test
	public void testSave_SavesNewConfiguration() {
		// Given
		Configuration configuration = new Configuration();
		configuration.setKey("configuration_key");
		configuration.setValue("configuration_value");

		ConfigurationMongoEntity configurationEntity = new ConfigurationMongoEntity();
		configurationEntity.setKey("configuration_key");
		configurationEntity.setValue("configuration_value");

		when(configurationRepository.findByKey("configuration_key")).thenReturn(Optional.empty());
		when(configurationMongoMapper.domain2Entity(configuration)).thenReturn(configurationEntity);

		// When
		saveConfigurationsUseCase.save(Arrays.asList(configuration));

		// Then
		verify(configurationRepository).save(configurationEntity);
	}

	@Test
	public void testSave_UpdatesExistingConfiguration() {
		// Given
		Configuration configuration = new Configuration();
		configuration.setKey("configuration_key");
		configuration.setValue("new_configuration_value");

		ConfigurationMongoEntity existingConfigurationEntity = new ConfigurationMongoEntity();
		existingConfigurationEntity.setKey("configuration_key");
		existingConfigurationEntity.setValue("old_configuration_value");

		ConfigurationMongoEntity updatedConfigurationEntity = new ConfigurationMongoEntity();
		updatedConfigurationEntity.setKey("configuration_key");
		updatedConfigurationEntity.setValue("new_configuration_value");

		when(configurationRepository.findByKey(anyString())).thenReturn(Optional.of(existingConfigurationEntity));
		when(configurationMongoMapper.entity2Domain(existingConfigurationEntity)).thenReturn(configuration);
		when(configurationMongoMapper.domain2Entity(configuration)).thenReturn(updatedConfigurationEntity);

		// When
		saveConfigurationsUseCase.save(Arrays.asList(configuration));

		// Then
		verify(configurationRepository, atLeast(1)).save(any());
	}

	@Test
	@Disabled
	public void testSave_LeavesUnchangedConfiguration() {
		// Given
		Configuration configuration = new Configuration();
		configuration.setKey("configuration_key");
		configuration.setValue("old_configuration_value");

		ConfigurationMongoEntity existingConfigurationEntity = new ConfigurationMongoEntity();
		existingConfigurationEntity.setKey("configuration_key");
		existingConfigurationEntity.setValue("old_configuration_value");

		when(configurationRepository.findByKey("configuration_key")).thenReturn(Optional.of(existingConfigurationEntity));
		when(configurationMongoMapper.entity2Domain(existingConfigurationEntity)).thenReturn(configuration);

		// When
		saveConfigurationsUseCase.save(Arrays.asList(configuration));

		// Then
		verify(configurationRepository, atLeast(1)).save(any());
	}

	@Test
	public void testSave_SavesMultipleConfigurations() {
		// Given
		Configuration configuration1 = new Configuration();
		configuration1.setKey("configuration_key1");
		configuration1.setValue("configuration_value1");

		Configuration configuration2 = new Configuration();
		configuration2.setKey("configuration_key2");
		configuration2.setValue("configuration_value2");

		ConfigurationMongoEntity configurationEntity1 = new ConfigurationMongoEntity();
		configurationEntity1.setKey("configuration_key1");
		configurationEntity1.setValue("configuration_value1");

		ConfigurationMongoEntity configurationEntity2 = new ConfigurationMongoEntity();
		configurationEntity2.setKey("configuration_key2");
		configurationEntity2.setValue("configuration_value2");

		when(configurationRepository.findByKey("configuration_key1")).thenReturn(Optional.empty());
		when(configurationRepository.findByKey("configuration_key2")).thenReturn(Optional.empty());
		when(configurationMongoMapper.domain2Entity(configuration1)).thenReturn(configurationEntity1);
		when(configurationMongoMapper.domain2Entity(configuration2)).thenReturn(configurationEntity2);

		// When
		saveConfigurationsUseCase.save(Arrays.asList(configuration1, configuration2));

		// Then
		verify(configurationRepository).save(configurationEntity1);
		verify(configurationRepository).save(configurationEntity2);
	}
}
