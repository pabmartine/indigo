package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.indigo.MyBook;
import com.martinia.indigo.repository.indigo.FavoriteBookRepository;
import com.martinia.indigo.repository.indigo.MyBookRepository;
import com.martinia.indigo.repository.indigo.NotificationRepository;
import com.martinia.indigo.services.calibre.BookService;
import com.martinia.indigo.services.indigo.MyBookService;
import com.martinia.indigo.utils.GoodReadsComponent;
import com.martinia.indigo.utils.GoogleBooksComponent;

@Service
public class MyBookServiceImpl implements MyBookService {

	@Autowired
	MyBookRepository myBookRepository;

	@Autowired
	BookService bookService;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	FavoriteBookRepository favoriteBookRepository;

	@Autowired
	GoodReadsComponent goodReadsService;

	@Autowired
	GoogleBooksComponent googleBooksService;

	@Override
	public List<Integer> getSentBooks(int user) {
		return notificationRepository.getSentBooks(user);
	}

	@Override
	public List<Integer> getFavoriteBooks(int user) {
		return favoriteBookRepository.findBooksByUser(user);
	}

	@Override
	public MyBook getBookInfoBy(int id, boolean local) {
		MyBook myBook = null;

		Optional<MyBook> optional = myBookRepository.findById(id);

		if (!optional.isPresent()) {
			if (!local) {
				Book book = bookService.findById(id)
						.get();

				myBook = goodReadsService.findBook(book.getTitle(), book.getAuthorSort());

				if (myBook == null) {
					myBook = googleBooksService.findBook(book.getTitle(), book.getAuthorSort());
				}

				if (myBook != null) {
					myBook.setId(book.getId());
					myBookRepository.save(myBook);
				}

			}
		} else {
			myBook = optional.get();
		}

		return myBook;
	}

}
