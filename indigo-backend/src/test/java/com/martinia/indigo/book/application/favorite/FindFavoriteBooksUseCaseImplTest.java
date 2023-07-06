package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.service.favorite.FindFavoriteBooksUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindFavoriteBooksUseCaseImplTest extends BaseIndigoTest {
	@MockBean
	private UserRepository userRepository;

	@Resource
	private FindFavoriteBooksUseCase useCase;

	@Test
	void testGetFavoriteBooks() {
		// Given
		String user = "example_user";
		List<Book> expectedBooks = Arrays.asList(new Book(), new Book());
		when(userRepository.getFavoriteBooks(user)).thenReturn(expectedBooks);

		// When
		List<Book> actualBooks = useCase.getFavoriteBooks(user);

		// Then
		assertEquals(expectedBooks, actualBooks);
		verify(userRepository, times(1)).getFavoriteBooks(user);
	}
}