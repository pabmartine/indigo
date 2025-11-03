package com.martinia.indigo.notification.domain.ports.repositories;

import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationMongoEntity, String> {

	@Query("{ 'user' : ?0 }")
	List<NotificationMongoEntity> findByUser(String user);

	List<NotificationMongoEntity> findByUserAndType(String user, String type);

	List<NotificationMongoEntity> findByUserAndReadUserIsFalse(String user);

	List<NotificationMongoEntity> findByReadAdminIsFalse();

}