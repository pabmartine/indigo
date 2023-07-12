package com.martinia.indigo.book.application.resource;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.application.sent.FindSentBooksUseCaseImpl;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.resource.ObtainBookByPathUseCase;
import com.martinia.indigo.common.util.UtilComponent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class ObtainBookByPathUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private UtilComponent utilComponent;

	@javax.annotation.Resource
	private ObtainBookByPathUseCase obtainBookByPathUseCase;

	@Test
	public void testGetEpub() {
		// Given
		String path = "examplePath";
		Resource expectedResource = Mockito.mock(Resource.class);

		// Mock the behavior of utilComponent.getEpub()
		when(utilComponent.getEpub(path.replace("@_@", "&").replace("@-@", "[").replace("@¡@", "]").replace("@!@", "`"))).thenReturn(
				expectedResource);

		// When
		Resource actualResource = obtainBookByPathUseCase.getEpub(path);

		// Then
		assertNotNull(actualResource);
		assertEquals(expectedResource, actualResource);
	}

	public static class FindSentBooksUseCaseImplTest extends BaseIndigoTest {

		@InjectMocks
		private FindSentBooksUseCaseImpl findSendBooksNotificationUseCase;

//		@MockBean
//		private NotificationRepository notificationRepository;

		@Test
		public void getSentBooks_NotificationsExist_ReturnsListOfBooks() {
			// Given
			String user = "john";

			Book book1 = new Book();
			book1.setId("1");

			Book book2 = new Book();
			book2.setId("2");

			List<Book> expectedBooks = Arrays.asList(book1, book2);

//			when(notificationRepository.getSentBooks(user)).thenReturn(expectedBooks);

			// When
			List<Book> result = findSendBooksNotificationUseCase.getSentBooks(user);

			// Then
			assertEquals(expectedBooks, result);
		}
	}
}
