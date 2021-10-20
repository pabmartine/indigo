package com.martinia.indigo.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.enums.NotificationEnum;
import com.martinia.indigo.domain.enums.StatusEnum;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Notification;
import com.martinia.indigo.ports.in.rest.NotificationService;
import com.martinia.indigo.ports.out.mongo.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	NotificationRepository notificationRepository;

	@Override
	public List<Notification> findAllByOrderBySendDateDesc() {
		return notificationRepository.findAllByOrderBySendDateDesc();
	}

	@Override
	public List<Notification> findByUser(String user) {
		return notificationRepository.findByUserAndType(user, NotificationEnum.KINDLE);
	}

	@Override
	public List<Notification> findByUserAndStatus(String user, StatusEnum status) {
		return notificationRepository.findByUserAndStatus(user, status);
	}

	@Override
	public List<Notification> findByStatus(StatusEnum status) {
		return notificationRepository.findByStatus(status);
	}

	@Override
	public List<Notification> findNotReadUser(String user) {
		return notificationRepository.findByUserAndReadUserFalse(user);
	}

	@Override
	public List<Notification> findNotReadAdmin() {
		return notificationRepository.findByReadAdminFalse();
	}

	@Override
	public void save(Notification notification) {
		notificationRepository.save(notification);
	}

	@Override
	public Notification findById(String id) {
		return notificationRepository.findById(id);
	}

	@Override
	public void delete(String id) {
		notificationRepository.delete(id);
	}

	@Override
	public void markAsRead(String id, String user) {
		notificationRepository.markAsRead(id, user);
	}

	@Override
	public List<Book> getSentBooks(String user) {
		return notificationRepository.getSentBooks(user);
	}

}
