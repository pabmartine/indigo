package com.martinia.indigo.common.configuration.application;

import com.martinia.indigo.common.configuration.domain.service.SaveConfigurationsUseCase;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
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
