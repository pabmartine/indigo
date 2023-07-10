package com.martinia.indigo.notification.domain.ports.usecases;

public interface MarkAsReadNotificationUseCase {

	void markAsRead(String id, String user);

}
