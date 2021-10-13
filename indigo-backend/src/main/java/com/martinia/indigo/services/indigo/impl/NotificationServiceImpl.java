package com.martinia.indigo.services.indigo.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martinia.indigo.enums.StatusEnum;
import com.martinia.indigo.model.indigo.Notification;
import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.repository.indigo.NotificationRepository;
import com.martinia.indigo.services.indigo.NotificationService;
import com.martinia.indigo.services.indigo.UserService;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	private UserService userService;

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
	public void delete(int id) {
		Notification notification = this.findById(id)
				.get();
		notificationRepository.delete(notification);
	}

	@Override
	public void markAsRead(int id, int user) {
		User usr = userService.findById(user)
				.get();
		Notification notification = this.findById(id)
				.get();
		if (usr.getRole()
				.equals("ADMIN"))
			notification.setReadByAdmin(true);
		else
			notification.setReadByUser(true);
		this.save(notification);
	}

}
