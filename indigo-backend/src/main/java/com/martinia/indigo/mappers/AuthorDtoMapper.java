package com.martinia.indigo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.dto.AuthorDto;
import com.martinia.indigo.model.indigo.MyAuthor;

/**
 * Mapping interface from Author to AuthorDTO
 *
 */
@Mapper(componentModel = "spring")
public interface AuthorDtoMapper {

	/**
	 * Transforms a myauthor object into a DTO
	 * 
	 * @param myauthor object
	 * @return dto
	 */

	AuthorDto myAuthorToAuthorDto(MyAuthor myauthor);

	/**
	 * Transforms a list of author objects into a list of DTOs
	 * 
	 * @param myAuthors objects
	 * @return the dtos
	 */
	List<AuthorDto> myAuthorsToAuthorDtos(List<MyAuthor> myAuthors);
}