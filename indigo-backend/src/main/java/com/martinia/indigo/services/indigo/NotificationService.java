package com.martinia.indigo.services.indigo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.enums.StatusEnum;
import com.martinia.indigo.model.indigo.Notification;

public interface NotificationService {

	List<Notification> findAllByOrderByIdDesc();

	List<Notification> findByUser(int user);

	List<Notification> findByUserAndStatus(int user, StatusEnum status);

	List<Notification> findByStatus(StatusEnum status);

	List<Notification> findNotReadUser(int user);

	List<Notification> findNotReadAdmin();

	void save(Notification notification);

	Optional<Notification> findById(int id);

	void delete(Notification notification);

}
