package com.martinia.indigo.repository.indigo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.model.indigo.Configuration;

@Repository
public interface ConfigurationRepository extends CrudRepository<Configuration, String> {
}
