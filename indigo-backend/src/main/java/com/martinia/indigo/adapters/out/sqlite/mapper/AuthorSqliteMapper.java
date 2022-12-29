package com.martinia.indigo.adapters.out.sqlite.mapper;

import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.sqlite.entities.AuthorSqliteEntity;
import com.martinia.indigo.domain.model.Author;

@Mapper(componentModel = "spring")
public interface AuthorSqliteMapper {

	Author entity2Domain(AuthorSqliteEntity entity);
	
	List<Author> entities2Domains(List<AuthorSqliteEntity> entities);
}