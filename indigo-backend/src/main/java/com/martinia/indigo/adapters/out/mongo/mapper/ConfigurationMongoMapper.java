package com.martinia.indigo.adapters.out.mongo.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.configuration.domain.model.Configuration;

@Mapper(componentModel = "spring")
public interface ConfigurationMongoMapper {

	ConfigurationMongoEntity domain2Entity(Configuration domain);

	Configuration entity2Domain(ConfigurationMongoEntity entity);

	List<ConfigurationMongoEntity> domain2Entity(List<Configuration> domains);

	List<Configuration> entity2Domain(List<ConfigurationMongoEntity> entities);

}