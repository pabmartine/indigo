package com.martinia.indigo.book.infrastructure.api.mappers;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.api.model.BookSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookSummaryDtoMapper {

	@Mappings({
			@Mapping(target = "pubDate", dateFormat = "dd/MM/yyyy")
	})
	BookSummaryDto domain2Dto(Book domain);

	List<BookSummaryDto> domains2Dtos(List<Book> domains);
}
