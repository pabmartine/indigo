package com.martinia.indigo.adapters.out.mongo.mapper;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.mongo.entities.ViewMongoEntity;
import com.martinia.indigo.common.model.View;

@Mapper(componentModel = "spring")
public interface ViewMongoMapper {

	ViewMongoEntity domain2Entity(View domain);

}