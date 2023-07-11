package com.martinia.indigo.configuration.infrastructure.api.mappers;

import com.martinia.indigo.configuration.infrastructure.api.model.ConfigurationDto;
import com.martinia.indigo.configuration.domain.model.Configuration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigurationDtoMapper {
    ConfigurationDto domain2Dto(Configuration domain);

}