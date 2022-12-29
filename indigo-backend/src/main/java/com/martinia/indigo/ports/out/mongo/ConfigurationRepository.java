package com.martinia.indigo.ports.out.mongo;

import com.martinia.indigo.domain.model.Configuration;

import java.util.Optional;

public interface ConfigurationRepository {

	void save(Configuration configuration);

	Optional<Configuration> findByKey(String key);

}
