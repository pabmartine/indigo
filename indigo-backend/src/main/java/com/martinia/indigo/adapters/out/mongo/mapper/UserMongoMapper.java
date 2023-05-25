package com.martinia.indigo.adapters.out.mongo.mapper;

import java.util.List;

import com.martinia.indigo.user.domain.model.User;
import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;

@Mapper(componentModel = "spring")
public interface UserMongoMapper {

	UserMongoEntity domain2Entity(User domain);

	User entity2Domain(UserMongoEntity entity);

	List<UserMongoEntity> domain2Entity(
			List<User> domains);

	List<User> entities2Domains(
			List<UserMongoEntity> entities);

}