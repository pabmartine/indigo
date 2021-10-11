package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.enums.StatusEnum;
import com.martinia.indigo.model.indigo.Notification;
import com.martinia.indigo.repository.indigo.NotificationRepository;
import com.martinia.indigo.services.indigo.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	NotificationRepository notificationRepository;

	@Override
	public List<Notification> findAllByOrderByIdDesc() {
		return notificationRepository.findAllByOrderByIdDesc();
	}

	@Override
	public List<Notification> findByUser(int user) {
		return notificationRepository.findByUser(user);
	}

	@Override
	public List<Notification> findByUserAndStatus(int user, StatusEnum status) {
		return notificationRepository.findByUserAndStatus(user, status);
	}

	@Override
	public List<Notification> findByStatus(StatusEnum status) {
		return notificationRepository.findByStatus(status);
	}

	@Override
	public List<Notification> findNotReadUser(int user) {
		return notificationRepository.findByUserAndReadByUserFalse(user);
	}

	@Override
	public List<Notification> findNotReadAdmin() {
		return notificationRepository.findByReadByAdminFalse();
	}

	@Override
	public void save(Notification notification) {
		notificationRepository.save(notification);
	}

	@Override
	public Optional<Notification> findById(int id) {
		return notificationRepository.findById(id);
	}

	@Override
	public void delete(Notification notification) {
		notificationRepository.delete(notification);
	}

}
