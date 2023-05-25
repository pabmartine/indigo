package com.martinia.indigo.common.configuration.domain.repository;

import com.martinia.indigo.common.configuration.domain.model.Configuration;

import java.util.Optional;

public interface ConfigurationRepository {

	void save(Configuration configuration);

	Optional<Configuration> findByKey(String key);

}
