package com.martinia.indigo.domain.services;

import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.in.rest.ConfigurationService;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    ConfigurationRepository configurationRepository;

    @Override
    public Optional<Configuration> findByKey(String key) {
        return configurationRepository.findByKey(key);
    }

    @Override
    public void save(List<Configuration> configurations) {

        for (Configuration configuration : configurations) {

            Optional<Configuration> _configuration = this.findByKey(configuration.getKey());


            _configuration.ifPresentOrElse(conf -> {
                if (conf.getValue() == null || !conf.getValue()
                        .equals(configuration.getValue())) {
                    conf.setValue(configuration.getValue());
                    configurationRepository.save(conf);
                }
            }, () -> configurationRepository.save(configuration));

        }

    }
}
