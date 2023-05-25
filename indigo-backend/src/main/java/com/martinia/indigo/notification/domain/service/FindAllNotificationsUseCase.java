package com.martinia.indigo.notification.domain.service;

import com.martinia.indigo.notification.domain.model.Notification;

import java.util.List;

public interface FindAllNotificationsUseCase {

	List<Notification> findAllByOrderBySendDateDesc();

}
