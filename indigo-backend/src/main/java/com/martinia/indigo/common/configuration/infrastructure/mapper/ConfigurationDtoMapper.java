package com.martinia.indigo.common.configuration.infrastructure.mapper;

import com.martinia.indigo.common.configuration.infrastructure.model.ConfigurationDto;
import com.martinia.indigo.common.configuration.domain.model.Configuration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigurationDtoMapper {
    ConfigurationDto domain2Dto(Configuration domain);

}