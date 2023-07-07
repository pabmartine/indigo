package com.martinia.indigo.book.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.martinia.indigo.book.infrastructure.model.BookDto;
import com.martinia.indigo.book.domain.model.Book;

@Mapper(componentModel = "spring")
public interface BookDtoMapper {

	@Mappings({
		@Mapping(target = "pubDate", dateFormat = "dd/MM/yyyy"),
		@Mapping(target = "lastModified", dateFormat = "dd/MM/yyyy")
	})
	BookDto domain2Dto(Book domain);

	List<BookDto> domains2Dtos(List<Book> domains);

}