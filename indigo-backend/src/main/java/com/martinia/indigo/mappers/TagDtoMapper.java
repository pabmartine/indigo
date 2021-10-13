package com.martinia.indigo.mappers;

import org.mapstruct.Mapper;

import com.martinia.indigo.dto.TagDto;
import com.martinia.indigo.model.calibre.Tag;
import com.martinia.indigo.model.indigo.MyTag;

/**
 * Mapping interface from User to UserDTO
 *
 */
@Mapper(componentModel = "spring")
public interface TagDtoMapper {

	/**
	 * Transforms a tag object into a DTO
	 * 
	 * @param price domain object
	 * @return dto
	 */

	TagDto tagToTagDto(Tag tag);

	/**
	 * Transforms a myTag object into a DTO
	 * 
	 * @param price domain object
	 * @return dto
	 */

	TagDto myTagToTagDto(MyTag myTag);

}