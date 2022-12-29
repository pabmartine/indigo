package com.martinia.indigo.adapters.in.rest.mappers;

import com.martinia.indigo.adapters.in.rest.dtos.ConfigurationDto;
import com.martinia.indigo.domain.model.Configuration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigurationDtoMapper {
    ConfigurationDto domain2Dto(Configuration domain);
}