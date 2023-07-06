package com.martinia.indigo.tag.application;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.verify;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.RenameTagUseCase;
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
