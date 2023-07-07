package com.martinia.indigo.notification.domain.repository;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.StatusEnum;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.notification.domain.model.Notification;

public interface NotificationRepository {

	List<Notification> findAllByOrderBySendDateDesc();

	List<Notification> findByUserAndType(String user, NotificationEnum type);

	List<Notification> findByUserAndStatus(String user, StatusEnum status);

	List<Notification> findByStatus(StatusEnum status);

	List<Notification> findByUserAndReadUserFalse(String user);

	List<Notification> findByReadAdminFalse();

	void save(Notification notification);

	Optional<Notification> findById(String id);

	void delete(String id);

	void markAsRead(String id, String user);

	List<Book> getSentBooks(String user);


}
