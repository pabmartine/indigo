package com.martinia.indigo.adapters.in.rest.mappers;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.ConfigurationDto;
import com.martinia.indigo.domain.model.Configuration;

@Mapper(componentModel = "spring")
public interface ConfigurationDtoMapper {
	ConfigurationDto domain2Dto(Configuration domain);
}