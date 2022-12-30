package com.martinia.indigo.ports.out.mongo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.enums.NotificationEnum;
import com.martinia.indigo.domain.enums.StatusEnum;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Notification;

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
