package com.martinia.indigo.ports.in.rest;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.enums.StatusEnum;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Notification;

public interface NotificationService {

	List<Notification> findAllByOrderBySendDateDesc();

	List<Notification> findByUser(String user);

	List<Notification> findByUserAndStatus(String user, StatusEnum status);

	List<Notification> findByStatus(StatusEnum status);

	List<Notification> findNotReadUser(String user);

	List<Notification> findNotReadAdmin();

	Optional<Notification> findById(String id);

	void delete(String id);

	void markAsRead(String id, String user);

	void save(Notification notification);

	List<Book> getSentBooks(String user);

}
