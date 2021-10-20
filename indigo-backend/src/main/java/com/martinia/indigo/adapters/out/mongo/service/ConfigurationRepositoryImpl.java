package com.martinia.indigo.adapters.out.mongo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.mapper.ConfigurationMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.ConfigurationMongoRepository;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;

@Component
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

	@Autowired
	ConfigurationMongoRepository configurationMongoRepository;

	@Autowired
	ConfigurationMongoMapper configurationMongoMapper;

	@Override
	public void save(Configuration configuration) {
		configurationMongoRepository.save(configurationMongoMapper.domain2Entity(configuration));
	}

	@Override
	public Configuration findByKey(String key) {
		return configurationMongoMapper.entity2Domain(configurationMongoRepository.findByKey(key));
	}

}
