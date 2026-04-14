package com.martinia.indigo.book.application.sent;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.sent.FindSentBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		List<NotificationMongoEntity> notifications = notificationRepository.findByUserAndType(user, "KINDLE");
		Set<String> bookPaths = notifications.stream()
				.map(NotificationMongoEntity::getKindle)
				.filter(java.util.Objects::nonNull)
				.map(notification -> notification.getBook())
				.filter(java.util.Objects::nonNull)
				.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

		List<BookMongoEntity> books = bookRepository.findByPathIn(new ArrayList<>(bookPaths));
		books.forEach(book -> map.put(book.getPath(), bookMongoMapper.entity2Domain(book)));
		return new ArrayList<>(map.values());
	}
}
