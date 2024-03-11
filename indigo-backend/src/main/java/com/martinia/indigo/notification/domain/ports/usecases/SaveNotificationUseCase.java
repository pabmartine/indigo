package com.martinia.indigo.notification.domain.ports.usecases;

import com.martinia.indigo.notification.domain.model.Notification;

public interface SaveNotificationUseCase {

	void save(Notification notification);

}
