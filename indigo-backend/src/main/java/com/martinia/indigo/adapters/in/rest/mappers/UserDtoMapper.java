package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.UserDto;
import com.martinia.indigo.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

	UserDto domain2Dto(User domain);

	List<UserDto> domains2Dtos(List<User> domains);
}