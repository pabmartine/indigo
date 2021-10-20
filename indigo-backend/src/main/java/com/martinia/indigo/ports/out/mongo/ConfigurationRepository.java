package com.martinia.indigo.ports.out.mongo;

import com.martinia.indigo.domain.model.Configuration;

public interface ConfigurationRepository {

	public void save(Configuration configuration);

	public Configuration findByKey(String key);

}
