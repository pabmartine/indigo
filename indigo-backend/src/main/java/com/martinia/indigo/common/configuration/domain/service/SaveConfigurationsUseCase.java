package com.martinia.indigo.common.configuration.domain.service;

import com.martinia.indigo.common.configuration.domain.model.Configuration;

import java.util.List;

public interface SaveConfigurationsUseCase {
	void save(final List<Configuration> configurations);

}
