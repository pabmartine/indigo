package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.TagDto;
import com.martinia.indigo.domain.model.Tag;

@Mapper(componentModel = "spring")
public interface TagDtoMapper {

	TagDto domain2Dto(Tag domain);

	List<TagDto> domains2Dtos(List<Tag> domains);

}