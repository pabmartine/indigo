package com.martinia.indigo.tag.application;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.verify;

import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.MergeTagUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class MergeTagUseCaseImplTest {

	@MockBean
	private TagRepository tagRepository;

	@Resource
	private MergeTagUseCase mergeTagUseCase;

	@Test
	public void testMerge() {
		// Given
		String sourceTag = "SourceTag";
		String targetTag = "TargetTag";

		// When
		mergeTagUseCase.merge(sourceTag, targetTag);

		// Then
		verify(tagRepository).merge(sourceTag, targetTag);
	}
}
