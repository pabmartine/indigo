package com.martinia.indigo.adapters.out.mongo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.mapper.ViewMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.ViewMongoRepository;
import com.martinia.indigo.common.model.View;
import com.martinia.indigo.ports.out.mongo.ViewRepository;

@Component
public class ViewRepositoryImpl implements ViewRepository {

	@Autowired
	ViewMongoRepository viewMongoRepository;

	@Autowired
	ViewMongoMapper viewMongoMapper;

	@Override
	public void save(View view) {
		viewMongoRepository.save(viewMongoMapper.domain2Entity(view));
	}

}
