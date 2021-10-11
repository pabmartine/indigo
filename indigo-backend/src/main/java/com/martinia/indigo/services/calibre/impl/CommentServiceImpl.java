package com.martinia.indigo.services.calibre.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.repository.calibre.CommentRepository;
import com.martinia.indigo.services.calibre.CommentService;

@Service
public class CommentServiceImpl implements CommentService{

	@Autowired
	CommentRepository commentRepository;
	
	@Override
	public String findTextByBookId(int id) {
		return commentRepository.findTextByBookId(id);
	}


}
