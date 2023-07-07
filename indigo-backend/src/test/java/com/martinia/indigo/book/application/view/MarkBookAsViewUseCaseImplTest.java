package com.martinia.indigo.book.application.view;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.service.view.MarkBookAsViewUseCase;
import com.martinia.indigo.common.model.View;
import com.martinia.indigo.ports.out.mongo.ViewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

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
		verify(viewRepository, times(1)).save(view);
	}

}