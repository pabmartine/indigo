package com.martinia.indigo.adapters.out.mongo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.common.infrastructure.mongo.mappers.ViewMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.ViewMongoRepository;
import com.martinia.indigo.common.model.View;
import com.martinia.indigo.book.domain.ports.repositories.ViewRepository;

import javax.annotation.Resource;

@Component
public class ViewRepositoryImpl implements ViewRepository {

	@Resource
	private ViewMongoRepository viewMongoRepository;

	@Resource
	private ViewMongoMapper viewMongoMapper;

	@Override
	public void save(View view) {
		viewMongoRepository.save(viewMongoMapper.domain2Entity(view));
	}

}
