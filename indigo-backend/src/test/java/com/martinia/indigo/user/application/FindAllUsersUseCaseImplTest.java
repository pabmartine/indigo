package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindAllUsersUseCaseImplTest extends BaseIndigoTest {

//	@Mock
//	private UserMongoRepository userMongoRepository;

	@InjectMocks
	private FindAllUsersUseCaseImpl findAllUsersUseCase;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testFindAll() {
		// Arrange
		List<User> expectedUsers = new ArrayList<>();
		expectedUsers.add(new User("1", "user1", "password1", "kindle1", "role1", "language1", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>()));
		expectedUsers.add(new User("2", "user2", "password2", "kindle2", "role2", "language2", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>()));
//		when(userRepository.findAll()).thenReturn(expectedUsers);

		// Act
		List<User> actualUsers = findAllUsersUseCase.findAll();

		// Assert
		assertEquals(expectedUsers.size(), actualUsers.size());
		for (int i = 0; i < expectedUsers.size(); i++) {
			User expectedUser = expectedUsers.get(i);
			User actualUser = actualUsers.get(i);
			assertEquals(expectedUser.getId(), actualUser.getId());
			assertEquals(expectedUser.getUsername(), actualUser.getUsername());
			assertEquals(expectedUser.getPassword(), actualUser.getPassword());
			assertEquals(expectedUser.getKindle(), actualUser.getKindle());
			assertEquals(expectedUser.getRole(), actualUser.getRole());
			assertEquals(expectedUser.getLanguage(), actualUser.getLanguage());
			assertEquals(expectedUser.getLanguageBooks(), actualUser.getLanguageBooks());
			assertEquals(expectedUser.getFavoriteBooks(), actualUser.getFavoriteBooks());
			assertEquals(expectedUser.getFavoriteAuthors(), actualUser.getFavoriteAuthors());
		}
	}
}