package com.martinia.indigo.configuration.infrastructure.mongo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.configuration.domain.model.Configuration;

@Mapper(componentModel = "spring")
public interface ConfigurationMongoMapper {

	ConfigurationMongoEntity domain2Entity(Configuration domain);

	Configuration entity2Domain(ConfigurationMongoEntity entity);

	List<ConfigurationMongoEntity> domain2Entity(List<Configuration> domains);

	List<Configuration> entity2Domain(List<ConfigurationMongoEntity> entities);

}