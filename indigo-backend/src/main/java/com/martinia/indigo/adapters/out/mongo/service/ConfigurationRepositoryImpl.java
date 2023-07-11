package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.configuration.infrastructure.mongo.mappers.ConfigurationMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.ConfigurationMongoRepository;
import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

    @Resource
    private ConfigurationMongoRepository configurationMongoRepository;

    @Resource
    private ConfigurationMongoMapper configurationMongoMapper;

    @Override
    public void save(Configuration configuration) {
        configurationMongoRepository.save(configurationMongoMapper.domain2Entity(configuration));
    }

    @Override
    public Optional<Configuration> findByKey(String key) {
        return configurationMongoRepository.findByKey(key).map(conf -> Optional.of(configurationMongoMapper.entity2Domain(conf))).orElse(Optional.empty());
    }

}
