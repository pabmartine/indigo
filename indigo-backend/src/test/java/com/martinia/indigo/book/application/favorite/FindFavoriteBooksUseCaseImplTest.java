package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.favorite.FindFavoriteBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FindFavoriteBooksUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindFavoriteBooksUseCase findFavoriteBooksUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testGetFavoriteBooks_ReturnsEmptyListWhenUserHasNoFavoriteBooks() {
		// Given
		String user = "john_doe";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteBooks(Collections.emptyList());

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));

		// When
		List<Book> favoriteBooks = findFavoriteBooksUseCase.getFavoriteBooks(user);

		// Then
		verify(userRepository).findByUsername(user);
		assert (favoriteBooks.isEmpty());
	}

	@Test
	public void testGetFavoriteBooks_ReturnsBooksWhenUserHasFavoriteBooks() {
		// Given
		String user = "john_doe";
		List<String> favoriteBookPaths = Arrays.asList("/books/1", "/books/2");

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setUsername(user);
		userEntity.setFavoriteBooks(favoriteBookPaths);

		BookMongoEntity book1 = new BookMongoEntity();
		book1.setPath("/books/1");

		BookMongoEntity book2 = new BookMongoEntity();
		book2.setPath("/books/2");

		when(userRepository.findByUsername(user)).thenReturn(Optional.of(userEntity));
		when(bookRepository.findByPath("/books/1")).thenReturn(Optional.of(book1));
		when(bookRepository.findByPath("/books/2")).thenReturn(Optional.of(book2));
		when(bookMongoMapper.entity2Domain(book1)).thenReturn(new Book());
		when(bookMongoMapper.entity2Domain(book2)).thenReturn(new Book());

		// When
		List<Book> favoriteBooks = findFavoriteBooksUseCase.getFavoriteBooks(user);

		// Then
		verify(userRepository).findByUsername(user);
		verify(bookRepository).findByPath("/books/1");
		verify(bookRepository).findByPath("/books/2");
		verify(bookMongoMapper).entity2Domain(book1);
		verify(bookMongoMapper).entity2Domain(book2);

		assert (favoriteBooks.size() == favoriteBookPaths.size());
	}
}
