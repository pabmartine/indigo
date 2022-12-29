package com.martinia.indigo.ports.in.rest;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.model.Configuration;

public interface ConfigurationService {

	Optional<Configuration> findByKey(String key);

	void save(List<Configuration> configurations);

}
