package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.mapper.ConfigurationMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.ConfigurationMongoRepository;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    public Optional<Configuration> findByKey(String key) {
        return configurationMongoRepository.findByKey(key).map(conf -> Optional.of(configurationMongoMapper.entity2Domain(conf))).orElse(Optional.empty());
    }

}
