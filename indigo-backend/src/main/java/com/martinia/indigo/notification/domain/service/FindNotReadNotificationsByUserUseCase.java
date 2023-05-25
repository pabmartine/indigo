package com.martinia.indigo.notification.domain.service;

import com.martinia.indigo.notification.domain.model.Notification;

import java.util.List;

public interface FindNotReadNotificationsByUserUseCase {

	List<Notification> findNotReadUser(String user);

}
