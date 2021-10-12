package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.calibre.Author;
import com.martinia.indigo.model.indigo.MyAuthor;
import com.martinia.indigo.repository.indigo.MyAuthorRepository;
import com.martinia.indigo.services.calibre.AuthorService;
import com.martinia.indigo.services.indigo.MyAuthorService;
import com.martinia.indigo.utils.GoodReadsComponent;
import com.martinia.indigo.utils.WikipediaComponent;

@Service
public class MyAuthorServiceImpl implements MyAuthorService {

	@Autowired
	MyAuthorRepository myAuthorRepository;

	@Autowired
	AuthorService authorService;

	@Autowired
	WikipediaComponent wikipediaService;

	@Autowired
	GoodReadsComponent goodReadsService;

	@Override
	public List<Integer> getFavoriteAuthors(int user) {
		return myAuthorRepository.getFavoriteAuthors(user);
	}

	@Override
	public Optional<MyAuthor> findById(int id) {
		return myAuthorRepository.findById(id);
	}

	@Override
	public MyAuthor getAuthorInfoByName(String author, String lang) {
		MyAuthor myAuthor = myAuthorRepository.findBySort(author);
		if (myAuthor == null) {

			myAuthor = wikipediaService.findAuthor(author, lang);

			if (myAuthor == null) {
				myAuthor = wikipediaService.findAuthor(author, "en");
			}

			if (myAuthor == null) {
				myAuthor = goodReadsService.findAuthor(author);
			}

			if (myAuthor != null) {
				myAuthor.setSort(author);
				Author auth = authorService.findBySort(author);
				myAuthor.setId(auth.getId());
				myAuthorRepository.save(myAuthor);
			}
		}
		return myAuthor;
	}

}
