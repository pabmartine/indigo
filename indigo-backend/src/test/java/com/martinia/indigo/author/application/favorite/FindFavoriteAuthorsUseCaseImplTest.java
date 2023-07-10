package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.usecases.favorite.FindFavoriteAuthorsUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindFavoriteAuthorsUseCaseImplTest extends BaseIndigoTest {
	@Resource
	private FindFavoriteAuthorsUseCase findFavoriteAuthorsUseCase;

	@MockBean
	private UserRepository userRepository;

	@Test
	void givenUser_whenGetFavoriteAuthors_thenReturnListOfAuthors() {
		// Given
		String user = "john";
		List<Author> favoriteAuthors = new ArrayList<>();
		favoriteAuthors.add(new Author());
		favoriteAuthors.add(new Author());
		when(userRepository.getFavoriteAuthors(user)).thenReturn(favoriteAuthors);

		// When
		List<Author> result = findFavoriteAuthorsUseCase.getFavoriteAuthors(user);

		// Then
		assertEquals(favoriteAuthors, result);
	}
}
