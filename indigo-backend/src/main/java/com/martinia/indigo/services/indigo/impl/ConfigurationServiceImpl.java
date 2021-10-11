package com.martinia.indigo.services.indigo.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.Configuration;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;
import com.martinia.indigo.services.indigo.ConfigurationService;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	ConfigurationRepository configurationRepository;

	@Override
	public Optional<Configuration> findById(String id) {
		return configurationRepository.findById(id);
	}

	@Override
	public void save(Configuration configuration) {
		configurationRepository.save(configuration);		
	}
}
