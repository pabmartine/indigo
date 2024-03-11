package com.martinia.indigo.notification.domain.ports.usecases;

import com.martinia.indigo.notification.domain.model.Notification;

import java.util.List;

public interface FindAllNotificationsUseCase {

	List<Notification> findAllByOrderBySendDateDesc();

}
