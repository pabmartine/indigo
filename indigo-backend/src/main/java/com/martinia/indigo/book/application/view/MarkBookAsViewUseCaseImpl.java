package com.martinia.indigo.book.application.view;

import com.martinia.indigo.book.domain.service.view.MarkBookAsViewUseCase;
import com.martinia.indigo.common.model.View;
import com.martinia.indigo.ports.out.mongo.ViewRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class MarkBookAsViewUseCaseImpl implements MarkBookAsViewUseCase {

	@Resource
	private ViewRepository viewRepository;

	@Override
	public void save(View view) {
		viewRepository.save(view);
	}

}
