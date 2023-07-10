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
import static org.mockito.Mockito.when;

public class FindSentBooksUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindSentBooksUseCase useCase;

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
		List<Book> result = useCase.getSentBooks(user);

		// Then
		assertEquals(expectedBooks, result);
	}
}
