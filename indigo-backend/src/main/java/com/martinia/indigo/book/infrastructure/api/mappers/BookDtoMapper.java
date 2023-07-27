package com.martinia.indigo.book.infrastructure.api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.book.domain.model.Book;

@Mapper(componentModel = "spring")
public interface BookDtoMapper {

	@Mappings({
		@Mapping(target = "pubDate", dateFormat = "dd/MM/yyyy"),
		@Mapping(target = "lastModified", dateFormat = "dd/MM/yyyy")
	})
	BookDto domain2Dto(Book domain);

	Book dto2domain(BookDto dto);

	List<BookDto> domains2Dtos(List<Book> domains);

}