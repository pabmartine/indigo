package com.martinia.indigo.configuration.application;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.domain.ports.usecases.FindConfigurationByKeyUseCase;
import com.martinia.indigo.configuration.infrastructure.mongo.mappers.ConfigurationMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindConfigurationByKeyUseCaseImpl implements FindConfigurationByKeyUseCase {

	@Resource
	private ConfigurationRepository configurationRepository;

	@Resource
	private ConfigurationMongoMapper configurationMongoMapper;

	@Override
	public Optional<Configuration> findByKey(String key) {
		return configurationRepository.findByKey(key).map(conf -> Optional.of(configurationMongoMapper.entity2Domain(conf)))
				.orElse(Optional.empty());
	}

}
