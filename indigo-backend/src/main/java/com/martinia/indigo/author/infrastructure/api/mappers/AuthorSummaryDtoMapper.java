package com.martinia.indigo.author.infrastructure.api.mappers;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.infrastructure.api.model.AuthorSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorSummaryDtoMapper {

	@Mappings({ @Mapping(source = "numBooks.total", target = "numBooks") })
	AuthorSummaryDto domain2Dto(Author domain);

	List<AuthorSummaryDto> domains2Dtos(List<Author> domains);

}
