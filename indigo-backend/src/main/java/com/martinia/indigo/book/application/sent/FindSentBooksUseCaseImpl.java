package com.martinia.indigo.book.application.sent;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.sent.FindSentBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class FindSentBooksUseCaseImpl implements FindSentBooksUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private BookRepository bookRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> getSentBooks(final String user) {
		Map<String, Book> map = new HashMap<>();
		List<NotificationMongoEntity> notifications = notificationRepository.findByUser(user);
		notifications.stream().filter(notification -> notification.getType().equals("KINDLE")).forEach(notification -> {
			if (!map.containsKey(notification.getKindle().getBook())) {
				Optional<BookMongoEntity> book = bookRepository.findByPath(notification.getKindle().getBook());
				if (book.isPresent()) {
					map.put(notification.getKindle().getBook(), bookMongoMapper.entity2Domain(book.get()));
				}
			}
		});
		return new ArrayList<>(map.values());
	}
}
