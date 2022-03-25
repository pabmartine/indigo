package com.martinia.indigo.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.in.rest.ConfigurationService;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	ConfigurationRepository configurationRepository;

	@Override
	public Configuration findByKey(String key) {
		return configurationRepository.findByKey(key);
	}

	@Override
	public void save(List<Configuration> configurations) {

		for (Configuration configuration : configurations) {
			Configuration _configuration = this.findByKey(configuration.getKey());

			if (_configuration == null) {
				configurationRepository.save(configuration);
			}

			if (_configuration != null && (_configuration.getValue() == null || !_configuration.getValue()
					.equals(configuration.getValue()))) {
				_configuration.setValue(configuration.getValue());
				configurationRepository.save(_configuration);
			}

		}

	}
}
