package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.martinia.indigo.adapters.in.rest.dtos.BookDto;
import com.martinia.indigo.domain.model.Book;

@Mapper(componentModel = "spring")
public interface BookDtoMapper {

	@Mappings({
		@Mapping(target = "pubDate", dateFormat = "dd/MM/yyyy"),
		@Mapping(target = "lastModified", dateFormat = "dd/MM/yyyy")
	})
	BookDto domain2Dto(Book domain);

	List<BookDto> domains2Dtos(List<Book> domains);

}