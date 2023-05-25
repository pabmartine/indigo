package com.martinia.indigo.common.configuration.application;

import com.martinia.indigo.common.configuration.domain.service.FindConfigurationByKeyUseCase;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindConfigurationByKeyUseCaseImpl implements FindConfigurationByKeyUseCase {

	@Resource
	private ConfigurationRepository configurationRepository;

	@Override
	public Optional<Configuration> findByKey(String key) {
		return configurationRepository.findByKey(key);
	}

}
