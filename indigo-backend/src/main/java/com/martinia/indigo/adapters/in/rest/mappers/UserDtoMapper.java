package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import com.martinia.indigo.user.domain.model.User;
import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.UserDto;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

	UserDto domain2Dto(User domain);

	List<UserDto> domains2Dtos(List<User> domains);
}