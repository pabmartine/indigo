package com.martinia.indigo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.dto.BookDto;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.MyBook;

/**
 * Mapping interface from Book to BookDTO
 *
 */
@Mapper(componentModel = "spring")
public interface BookDtoMapper {

	/**
	 * Transforms a book object into a DTO
	 * 
	 * @param book object
	 * @return dto
	 */

	BookDto bookToBookDto(Book book);

	/**
	 * Transforms a mybook object into a DTO
	 * 
	 * @param mybook object
	 * @return dto
	 */

	BookDto myBookToBookDto(MyBook mybook);

	/**
	 * Transforms a list of book objects into a list of DTOs
	 * 
	 * @param books objects
	 * @return the dtos
	 */
	List<BookDto> booksToBookDtos(List<Book> books);

	/**
	 * Transforms a list of book objects into a list of DTOs
	 * 
	 * @param myBooks objects
	 * @return the dtos
	 */
	List<BookDto> myBooksToBookDtos(List<MyBook> myBooks);
}