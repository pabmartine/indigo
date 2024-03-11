package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.usecases.UpdateImageTagUseCase;
import com.martinia.indigo.tag.infrastructure.api.mappers.TagDtoMapper;
import com.martinia.indigo.tag.infrastructure.api.model.TagDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UpdateImageTagControllerTest extends BaseIndigoTest {

	@Resource
	private UpdateImageTagController controller;

	@MockBean
	private UpdateImageTagUseCase useCase;

	@MockBean
	private TagDtoMapper mapper;

	@Test
	public void testUpdateImageWithValidSource() {
		// given
		String source = "valid_source";
		Tag tag = new com.martinia.indigo.tag.domain.model.Tag(); // You need to create a Tag instance for mocking
		TagDto expectedTagDto = new TagDto(); // You need to create a TagDto instance for mocking

		when(useCase.updateImage(source)).thenReturn(tag);
		when(mapper.domain2Dto(tag)).thenReturn(expectedTagDto);

		// when
		ResponseEntity<TagDto> response = controller.updateImage(source);

		// then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(expectedTagDto, response.getBody());

		verify(useCase).updateImage(source);
		verify(mapper).domain2Dto(tag);
	}

	@Test
	public void testUpdateImageWithInvalidSource() {
		// given
		String source = "invalid_source";

		when(useCase.updateImage(source)).thenReturn(null);

		// when
		ResponseEntity<TagDto> response = controller.updateImage(source);

		// then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNull(response.getBody());

		verify(useCase).updateImage(source);
		verifyNoInteractions(mapper);
	}

}