package com.martinia.indigo.book.application.view;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.ViewRepository;
import com.martinia.indigo.book.domain.ports.usecases.view.MarkBookAsViewUseCase;
import com.martinia.indigo.common.model.View;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MarkBookAsViewUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private ViewRepository viewRepository;
	@Resource
	private MarkBookAsViewUseCase useCase;

	@Test
	void testSave() {
		// Given
		View view = new View();
		// When
		useCase.save(view);

		// Then
		verify(viewRepository, times(1)).save(any());
	}

}