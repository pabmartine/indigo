package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.AuthorDto;
import com.martinia.indigo.domain.model.Author;

@Mapper(componentModel = "spring")
public interface AuthorDtoMapper {

	AuthorDto domain2Dto(Author domain);

	List<AuthorDto> domains2Dtos(List<Author> domains);

}