package com.martinia.indigo.tag.application;

import static org.mockito.Mockito.verify;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.RenameTagUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;


public class RenameTagUseCaseImplTest  extends BaseIndigoTest {

	@MockBean
	private TagRepository tagRepository;

	@Resource
	private RenameTagUseCase renameTagUseCase;

	@Test
	public void testRename() {
		// Given
		String sourceTag = "SourceTag";
		String targetTag = "TargetTag";

		// When
		renameTagUseCase.rename(sourceTag, targetTag);

		// Then
		verify(tagRepository).rename(sourceTag, targetTag);
	}
}
