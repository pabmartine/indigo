package com.martinia.indigo.services.indigo.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.indigo.MyTag;
import com.martinia.indigo.repository.indigo.MyTagRepository;
import com.martinia.indigo.services.indigo.MyTagService;

@Service
public class MyTagServiceImpl implements MyTagService {

	@Autowired
	MyTagRepository myTagRepository;

	public Optional<MyTag> findById(int source) {
		return myTagRepository.findById(null);
	}

	public void save(MyTag tag) {
		myTagRepository.save(tag);
	}

}
