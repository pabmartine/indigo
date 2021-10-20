package com.martinia.indigo.adapters.out.mongo.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;
import com.martinia.indigo.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserMongoMapper {

	UserMongoEntity domain2Entity(User domain);

	com.martinia.indigo.domain.model.User entity2Domain(UserMongoEntity entity);

	List<UserMongoEntity> domain2Entity(
			List<com.martinia.indigo.domain.model.User> domains);

	List<com.martinia.indigo.domain.model.User> entities2Domains(
			List<UserMongoEntity> entities);

}