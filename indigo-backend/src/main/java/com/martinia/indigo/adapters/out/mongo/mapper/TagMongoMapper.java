package com.martinia.indigo.adapters.out.mongo.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.mongo.entities.TagMongoEntity;
import com.martinia.indigo.domain.model.Tag;

@Mapper(componentModel = "spring")
public interface TagMongoMapper {

	TagMongoEntity domain2Entity(Tag domain);

	Tag entity2Domain(TagMongoEntity entity);

	List<TagMongoEntity> domains2Entities(List<Tag> domains);

	List<Tag> entities2Domains(List<TagMongoEntity> entities);

}