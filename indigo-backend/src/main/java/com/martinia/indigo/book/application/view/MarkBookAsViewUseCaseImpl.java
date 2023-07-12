package com.martinia.indigo.book.application.view;

import com.martinia.indigo.book.domain.ports.repositories.ViewRepository;
import com.martinia.indigo.book.domain.ports.usecases.view.MarkBookAsViewUseCase;
import com.martinia.indigo.common.infrastructure.mongo.mappers.ViewMongoMapper;
import com.martinia.indigo.common.model.View;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class MarkBookAsViewUseCaseImpl implements MarkBookAsViewUseCase {

	@Resource
	private ViewRepository viewRepository;

	@Resource
	private ViewMongoMapper viewMongoMapper;

	@Override
	public void save(View view) {
		viewRepository.save(viewMongoMapper.domain2Entity(view));
	}

}
