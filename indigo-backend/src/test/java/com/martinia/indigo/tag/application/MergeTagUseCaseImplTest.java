package com.martinia.indigo.tag.application;

import static org.mockito.Mockito.verify;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;


public class MergeTagUseCaseImplTest extends BaseIndigoTest {

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
