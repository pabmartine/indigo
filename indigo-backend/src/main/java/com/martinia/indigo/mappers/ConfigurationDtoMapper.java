package com.martinia.indigo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.dto.ConfigurationDto;
import com.martinia.indigo.model.indigo.Configuration;

/**
 * Mapping interface from Configuration to ConfigurationDTO
 *
 */
@Mapper(componentModel = "spring")
public interface ConfigurationDtoMapper {

	/**
	 * Transforms a configuration object into a DTO
	 * 
	 * @param price domain object
	 * @return dto
	 */

	ConfigurationDto configurationToConfigurationDto(Configuration configuration);

	/**
	 * Transforms a list of configuration objects into a list of DTOs
	 * 
	 * @param prices the domain objects
	 * @return the dtos
	 */
	List<ConfigurationDto> configurationsToConfigurationDtos(List<Configuration> configurations);
}