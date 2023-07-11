package com.martinia.indigo.tag.infrastructure.mongo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.domain.model.Tag;

@Mapper(componentModel = "spring")
public interface TagMongoMapper {

	TagMongoEntity domain2Entity(Tag domain);

	Tag entity2Domain(TagMongoEntity entity);

	List<TagMongoEntity> domains2Entities(List<Tag> domains);

	List<Tag> entities2Domains(List<TagMongoEntity> entities);

}