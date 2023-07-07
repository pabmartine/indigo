package com.martinia.indigo.configuration.domain.repository;

import com.martinia.indigo.configuration.domain.model.Configuration;

import java.util.Optional;

public interface ConfigurationRepository {

	void save(Configuration configuration);

	Optional<Configuration> findByKey(String key);

}
