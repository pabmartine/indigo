package com.martinia.indigo.adapters.out.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.martinia.indigo.adapters.out.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.domain.enums.StatusEnum;

public interface NotificationMongoRepository extends MongoRepository<NotificationMongoEntity, String> {

	List<NotificationMongoEntity> findAllByOrderBySendDateDesc();

	@Query("{ 'user' : ?0 }")
	List<NotificationMongoEntity> findByUser(String user);

	List<NotificationMongoEntity> findByUserAndStatus(String user, StatusEnum status);

	List<NotificationMongoEntity> findByStatus(StatusEnum status);

	List<NotificationMongoEntity> findByUserAndReadUserIsFalse(String user);

	List<NotificationMongoEntity> findByReadAdminIsFalse();

}