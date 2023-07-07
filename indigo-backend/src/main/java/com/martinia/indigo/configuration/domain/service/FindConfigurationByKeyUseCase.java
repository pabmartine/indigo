package com.martinia.indigo.configuration.domain.service;

import com.martinia.indigo.configuration.domain.model.Configuration;

import java.util.Optional;

public interface FindConfigurationByKeyUseCase {
	Optional<Configuration> findByKey(final String key);

}
