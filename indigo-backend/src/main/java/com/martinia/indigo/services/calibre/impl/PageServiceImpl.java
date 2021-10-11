package com.martinia.indigo.services.calibre.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.repository.calibre.PageRepository;
import com.martinia.indigo.services.calibre.PageService;

@Service
public class PageServiceImpl implements PageService {

	@Autowired
	PageRepository pageRepository;

	@Override
	public int findPagesByBookId(int id) {
		return pageRepository.findPagesByBookId(id);
	}

}
