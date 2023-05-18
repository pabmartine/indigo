package com.martinia.indigo.common.configuration.domain.service;

import com.martinia.indigo.domain.model.Configuration;

import java.util.List;
import java.util.Optional;

public interface SaveConfigurationsUseCase {
	void save(final List<Configuration> configurations);

}
