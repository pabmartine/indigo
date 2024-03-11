package com.martinia.indigo.user.infrastructure.api.mappers;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.infrastructure.api.model.UserDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserDtoMapperImplTest {

	private final UserDtoMapper userDtoMapper = new UserDtoMapperImpl();

	@Test
	public void testDomain2Dto_NullDomain_ReturnsNull() {
		// Given
		User domain = null;

		// When
		UserDto dto = userDtoMapper.domain2Dto(domain);

		// Then
		assertNull(dto);
	}

	@Test
	public void testDomain2Dto_NonNullDomain_ReturnsDtoWithMappedValues() {
		// Given
		User domain = new User();
		domain.setId("1");
		domain.setUsername("john_doe");
		domain.setPassword("password");
		domain.setKindle("my_kindle");
		domain.setRole("user");
		domain.setLanguage("en");
		List<String> languageBooks = new ArrayList<>();
		languageBooks.add("en");
		languageBooks.add("fr");
		domain.setLanguageBooks(languageBooks);

		// When
		UserDto dto = userDtoMapper.domain2Dto(domain);

		// Then
		assertEquals(domain.getId(), dto.getId());
		assertEquals(domain.getUsername(), dto.getUsername());
		assertEquals(domain.getPassword(), dto.getPassword());
		assertEquals(domain.getKindle(), dto.getKindle());
		assertEquals(domain.getRole(), dto.getRole());
		assertEquals(domain.getLanguage(), dto.getLanguage());
		assertEquals(domain.getLanguageBooks(), dto.getLanguageBooks());
	}

	@Test
	public void testDomains2Dtos_NullDomains_ReturnsNull() {
		// Given
		List<User> domains = null;

		// When
		List<UserDto> dtos = userDtoMapper.domains2Dtos(domains);

		// Then
		assertNull(dtos);
	}

	@Test
	public void testDomains2Dtos_NonNullDomains_ReturnsListOfMappedDtos() {
		// Given
		List<User> domains = new ArrayList<>();
		User domain1 = new User();
		domain1.setId("1");
		User domain2 = new User();
		domain2.setId("2");
		domains.add(domain1);
		domains.add(domain2);

		// When
		List<UserDto> dtos = userDtoMapper.domains2Dtos(domains);

		// Then
		assertEquals(domains.size(), dtos.size());
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(domains.get(i).getId(), dtos.get(i).getId());
		}
	}
}
