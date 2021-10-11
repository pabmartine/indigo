package com.martinia.indigo.repository.indigo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.martinia.indigo.enums.StatusEnum;
import com.martinia.indigo.model.indigo.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {

	List<Notification> findAllByOrderByIdDesc();
	
	List<Notification> findByUser(int user);
	
	List<Notification> findByUserAndStatus(int user, StatusEnum status);
	
	List<Notification> findByStatus(StatusEnum status);
	
	List<Notification> findByUserAndReadByUserFalse(int user);
	
	List<Notification> findByReadByAdminFalse();
	
	@Query("select distinct n.book from Notification n where n.user = :user")
	List<Integer> getSentBooks(@Param("user") int user);

}
