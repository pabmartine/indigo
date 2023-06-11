package com.martinia.indigo.notification.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class FindSendBooksNotificationUseCaseImplTest {

	@InjectMocks
	private FindSendBooksNotificationUseCaseImpl findSendBooksNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void getSentBooks_NotificationsExist_ReturnsListOfBooks() {
		// Given
		String user = "john";

		Book book1 = new Book();
		book1.setId("1");

		Book book2 = new Book();
		book2.setId("2");

		List<Book> expectedBooks = Arrays.asList(book1, book2);

		when(notificationRepository.getSentBooks(user)).thenReturn(expectedBooks);

		// When
		List<Book> result = findSendBooksNotificationUseCase.getSentBooks(user);

		// Then
		assertEquals(expectedBooks, result);
	}
}
