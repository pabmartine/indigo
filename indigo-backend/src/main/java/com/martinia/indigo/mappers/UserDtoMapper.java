package com.martinia.indigo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.dto.UserDto;
import com.martinia.indigo.model.indigo.User;

/**
 * Mapping interface from User to UserDTO
 *
 */
@Mapper(componentModel = "spring")
public interface UserDtoMapper {

	/**
	 * Transforms a user object into a DTO
	 * 
	 * @param price domain object
	 * @return dto
	 */

	UserDto userToUserDto(User user);

	/**
	 * Transforms a list of user objects into a list of DTOs
	 * 
	 * @param prices the domain objects
	 * @return the dtos
	 */
	List<UserDto> usersToUserDtos(List<User> users);
}