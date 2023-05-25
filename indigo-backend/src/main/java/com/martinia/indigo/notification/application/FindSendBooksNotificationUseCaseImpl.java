package com.martinia.indigo.notification.application;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import com.martinia.indigo.notification.domain.service.FindSendBooksNotificationUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindSendBooksNotificationUseCaseImpl implements FindSendBooksNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public List<Book> getSentBooks(final String user) {
		return notificationRepository.getSentBooks(user);
	}
}
