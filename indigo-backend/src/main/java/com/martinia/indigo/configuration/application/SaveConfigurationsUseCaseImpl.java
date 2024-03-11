package com.martinia.indigo.configuration.application;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.domain.ports.usecases.SaveConfigurationsUseCase;
import com.martinia.indigo.configuration.infrastructure.mongo.mappers.ConfigurationMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SaveConfigurationsUseCaseImpl implements SaveConfigurationsUseCase {

	@Resource
	private ConfigurationRepository configurationRepository;

	@Resource
	private ConfigurationMongoMapper configurationMongoMapper;

	@Override
	public void save(List<Configuration> configurations) {

		configurations.forEach(configuration -> {

			Optional<Configuration> _configuration = configurationRepository.findByKey(configuration.getKey())
					.map(conf -> Optional.of(configurationMongoMapper.entity2Domain(conf)))
					.orElse(Optional.empty());

			_configuration.ifPresentOrElse(conf -> {
				if (conf.getValue() == null || !conf.getValue().equals(configuration.getValue())) {
					conf.setValue(configuration.getValue());
					configurationRepository.save(configurationMongoMapper.domain2Entity(conf));
				}
			}, () -> configurationRepository.save(configurationMongoMapper.domain2Entity(configuration)));

		});

	}

}
