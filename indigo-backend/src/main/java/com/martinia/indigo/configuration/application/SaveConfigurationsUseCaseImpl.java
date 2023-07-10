package com.martinia.indigo.configuration.application;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.usecases.SaveConfigurationsUseCase;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class SaveConfigurationsUseCaseImpl implements SaveConfigurationsUseCase {

	@Resource
	private ConfigurationRepository configurationRepository;

	@Override
	public void save(List<Configuration> configurations) {

		for (Configuration configuration : configurations) {

			Optional<Configuration> _configuration = configurationRepository.findByKey(configuration.getKey());

			_configuration.ifPresentOrElse(conf -> {
				if (conf.getValue() == null || !conf.getValue().equals(configuration.getValue())) {
					conf.setValue(configuration.getValue());
					configurationRepository.save(conf);
				}
			}, () -> configurationRepository.save(configuration));

		}

	}

}
