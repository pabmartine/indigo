package com.martinia.indigo.services.indigo;

import java.util.Optional;

import com.martinia.indigo.model.indigo.Configuration;

public interface ConfigurationService {

	Optional<Configuration> findById(String string);

	void save(Configuration configuration);
}
