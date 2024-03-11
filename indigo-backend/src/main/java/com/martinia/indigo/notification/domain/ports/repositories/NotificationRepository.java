package com.martinia.indigo.notification.domain.ports.repositories;

import com.martinia.indigo.notification.domain.model.StatusEnum;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationMongoEntity, String> {

	List<NotificationMongoEntity> findAllByOrderBySendDateDesc();

	@Query("{ 'user' : ?0 }")
	List<NotificationMongoEntity> findByUser(String user);

	List<NotificationMongoEntity> findByUserAndStatus(String user, StatusEnum status);

	List<NotificationMongoEntity> findByUserAndType(String user, String type);

	List<NotificationMongoEntity> findByStatus(StatusEnum status);

	List<NotificationMongoEntity> findByUserAndReadUserIsFalse(String user);

	List<NotificationMongoEntity> findByReadAdminIsFalse();

}