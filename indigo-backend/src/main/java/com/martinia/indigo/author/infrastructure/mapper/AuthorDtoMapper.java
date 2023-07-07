package com.martinia.indigo.author.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.domain.model.Author;

@Mapper(componentModel = "spring")
public interface AuthorDtoMapper {

	@Mappings({ @Mapping(source = "numBooks.total", target = "numBooks") })
	AuthorDto domain2Dto(Author domain);

	List<AuthorDto> domains2Dtos(List<Author> domains);

}