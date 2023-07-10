package com.martinia.indigo.book.application.sent;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.usecases.sent.FindSentBooksUseCase;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindSentBooksUseCaseImpl implements FindSentBooksUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public List<Book> getSentBooks(final String user) {
		return notificationRepository.getSentBooks(user);
	}
}
