package com.martinia.indigo.user.infrastructure.api.mappers;

import java.util.List;

import com.martinia.indigo.user.domain.model.User;
import org.mapstruct.Mapper;

import com.martinia.indigo.user.infrastructure.api.model.UserDto;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

	UserDto domain2Dto(User domain);

	List<UserDto> domains2Dtos(List<User> domains);
}