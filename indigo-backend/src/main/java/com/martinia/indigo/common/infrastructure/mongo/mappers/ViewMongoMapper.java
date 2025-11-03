package com.martinia.indigo.common.infrastructure.mongo.mappers;

import org.mapstruct.Mapper;

import com.martinia.indigo.common.infrastructure.mongo.entities.ViewMongoEntity;
import com.martinia.indigo.common.domain.model.View;

@Mapper(componentModel = "spring")
public interface ViewMongoMapper {

	ViewMongoEntity domain2Entity(View domain);

}