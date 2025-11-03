package com.martinia.indigo.tag.infrastructure.api.mappers;

import com.martinia.indigo.common.domain.model.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagDtoMapperImplTest {

	private final TagDtoMapper tagDtoMapper = new TagDtoMapperImpl();

	@Test
	public void testDomain2Dto_NullDomain_ReturnsNull() {
		// Given
		Tag domain = null;

		// When
		TagDto dto = tagDtoMapper.domain2Dto(domain);

		// Then
		assertNull(dto);
	}

	@Test
	public void testDomain2Dto_NonNullDomain_ReturnsDtoWithMappedValues() {
		// Given
		Tag domain = new Tag();
		domain.setId("1");
		domain.setName("fiction");
		domain.setImage("fiction.jpg");
		NumBooks numBooks = new NumBooks();
		numBooks.setTotal(100);
		domain.setNumBooks(numBooks);

		// When
		TagDto dto = tagDtoMapper.domain2Dto(domain);

		// Then
		assertEquals(domain.getNumBooks().getTotal(), dto.getNumBooks());
		assertEquals(domain.getId(), dto.getId());
		assertEquals(domain.getName(), dto.getName());
		assertEquals(domain.getImage(), dto.getImage());
	}

	@Test
	public void testDomains2Dtos_NullDomains_ReturnsNull() {
		// Given
		List<Tag> domains = null;

		// When
		List<TagDto> dtos = tagDtoMapper.domains2Dtos(domains);

		// Then
		assertNull(dtos);
	}

	@Test
	public void testDomains2Dtos_NonNullDomains_ReturnsListOfMappedDtos() {
		// Given
		List<Tag> domains = new ArrayList<>();
		Tag domain1 = new Tag();
		domain1.setId("1");
		Tag domain2 = new Tag();
		domain2.setId("2");
		domains.add(domain1);
		domains.add(domain2);

		// When
		List<TagDto> dtos = tagDtoMapper.domains2Dtos(domains);

		// Then
		assertEquals(domains.size(), dtos.size());
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(domains.get(i).getId(), dtos.get(i).getId());
		}
	}
}
