package com.martinia.indigo.ports.in.rest;

import java.util.List;

import com.martinia.indigo.domain.model.Configuration;

public interface ConfigurationService {

	Configuration findByKey(String key);

	void save(List<Configuration> configurations);

}
