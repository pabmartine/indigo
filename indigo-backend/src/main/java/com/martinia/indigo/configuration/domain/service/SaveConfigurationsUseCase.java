package com.martinia.indigo.configuration.domain.service;

import com.martinia.indigo.configuration.domain.model.Configuration;

import java.util.List;

public interface SaveConfigurationsUseCase {
	void save(final List<Configuration> configurations);

}
