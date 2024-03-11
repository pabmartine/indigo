package com.martinia.indigo.book.application.sent;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.sent.FindSentBooksUseCase;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;

public class FindSentBooksUseCaseImplTest extends BaseIndigoTest{

	@Resource
	private FindSentBooksUseCase findSentBooksUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@Test
	public void testGetSentBooks_ReturnsSentBooks() {
		// Given
		String user = "john_doe";

		NotificationMongoEntity notification1 = new NotificationMongoEntity();
		notification1.setBook("Book 1");

		NotificationMongoEntity notification2 = new NotificationMongoEntity();
		notification2.setBook("Book 2");

		List<NotificationMongoEntity> notifications = new ArrayList<>();
		notifications.add(notification1);
		notifications.add(notification2);

		when(notificationRepository.findByUser(user)).thenReturn(notifications);

		BookMongoEntity book1 = new BookMongoEntity();
		book1.setId("1");
		book1.setTitle("Book 1");

		BookMongoEntity book2 = new BookMongoEntity();
		book2.setId("2");
		book2.setTitle("Book 2");

		when(bookRepository.findByPath("Book 1")).thenReturn(Optional.of(book1));
		when(bookRepository.findByPath("Book 2")).thenReturn(Optional.of(book2));

		when(bookMongoMapper.entity2Domain(book1)).thenReturn(new Book());
		when(bookMongoMapper.entity2Domain(book2)).thenReturn(new Book());

		// When
		List<Book> result = findSentBooksUseCase.getSentBooks(user);

		// Then
		assertEquals(2, result.size());
	}

	@Test
	public void testGetSentBooks_ReturnsEmptyListWhenNoNotifications() {
		// Given
		String user = "john_doe";

		when(notificationRepository.findByUser(user)).thenReturn(new ArrayList<>());

		// When
		List<Book> result = findSentBooksUseCase.getSentBooks(user);

		// Then
		assertEquals(0, result.size());
	}
}

