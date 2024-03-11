package com.martinia.indigo.configuration.domain.ports.usecases;

import com.martinia.indigo.configuration.domain.model.Configuration;

import java.util.List;

public interface SaveConfigurationsUseCase {
	void save(final List<Configuration> configurations);

}
