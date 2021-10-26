package com.martinia.indigo.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.model.View;
import com.martinia.indigo.ports.in.rest.ViewService;
import com.martinia.indigo.ports.out.mongo.ViewRepository;

@Service
public class ViewServiceImpl implements ViewService {

	@Autowired
	ViewRepository viewRepository;

	@Override
	public void save(View view) {
		viewRepository.save(view);
	}

}
