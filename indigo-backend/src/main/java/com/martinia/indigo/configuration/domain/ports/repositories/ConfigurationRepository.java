package com.martinia.indigo.configuration.domain.ports.repositories;

import com.martinia.indigo.configuration.domain.model.Configuration;

import java.util.Optional;

public interface ConfigurationRepository {

	void save(Configuration configuration);

	Optional<Configuration> findByKey(String key);

}
