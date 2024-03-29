package com.martinia.indigo.tag.infrastructure.api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import com.martinia.indigo.tag.domain.model.Tag;

@Mapper(componentModel = "spring")
public interface TagDtoMapper {

	@Mappings({ @Mapping(source = "numBooks.total", target = "numBooks") })
	TagDto domain2Dto(Tag domain);

	List<TagDto> domains2Dtos(List<Tag> domains);

}