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
		return myTagRepository.findById(source);
	}

	@Override
	public void image(int source, String image) {
		Optional<MyTag> optional = this.findById(source);
		MyTag tag = null;
		if (optional.isPresent()) {
			tag = optional.get();
			tag.setImage(image);
		} else {
			tag = new MyTag(source, image);
		}
		myTagRepository.save(tag);
	}

}
