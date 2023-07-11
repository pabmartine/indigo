package com.martinia.indigo.configuration.application;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationMongoRepository;
import com.martinia.indigo.configuration.domain.ports.usecases.SaveConfigurationsUseCase;
import com.martinia.indigo.configuration.infrastructure.mongo.mappers.ConfigurationMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class SaveConfigurationsUseCaseImpl implements SaveConfigurationsUseCase {

	@Resource
	private ConfigurationMongoRepository configurationMongoRepository;

	@Resource
	private ConfigurationMongoMapper configurationMongoMapper;

	@Override
	public void save(List<Configuration> configurations) {

		for (Configuration configuration : configurations) {

			Optional<Configuration> _configuration = configurationMongoRepository.findByKey(configuration.getKey())
					.map(conf -> Optional.of(configurationMongoMapper.entity2Domain(conf))).orElse(Optional.empty());

			_configuration.ifPresentOrElse(conf -> {
				if (conf.getValue() == null || !conf.getValue().equals(configuration.getValue())) {
					conf.setValue(configuration.getValue());
					configurationMongoRepository.save(configurationMongoMapper.domain2Entity(conf));
				}
			}, () -> configurationMongoRepository.save(configurationMongoMapper.domain2Entity(configuration)));

		}

	}

}
