package com.martinia.indigo.common.configuration.domain.service;

import com.martinia.indigo.domain.model.Configuration;

import java.util.Optional;

public interface FindConfigurationByKeyUseCase {
	Optional<Configuration> findByKey(final String key);

}
