package com.martinia.indigo.author.infrastructure.mongo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.domain.model.Author;

@Mapper(componentModel = "spring")
public interface AuthorMongoMapper {

	AuthorMongoEntity domain2Entity(Author domain);

	Author entity2Domain(AuthorMongoEntity entity);

	List<AuthorMongoEntity> domain2Entity(List<Author> domain);

	List<Author> entities2Domains(List<AuthorMongoEntity> entities);

}