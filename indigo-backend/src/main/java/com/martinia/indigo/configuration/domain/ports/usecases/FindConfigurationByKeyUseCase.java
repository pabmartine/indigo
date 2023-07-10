package com.martinia.indigo.configuration.domain.ports.usecases;

import com.martinia.indigo.configuration.domain.model.Configuration;

import java.util.Optional;

public interface FindConfigurationByKeyUseCase {
	Optional<Configuration> findByKey(final String key);

}
