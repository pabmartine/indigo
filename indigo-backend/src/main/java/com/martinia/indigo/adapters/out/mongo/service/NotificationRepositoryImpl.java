package com.martinia.indigo.adapters.out.mongo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.adapters.out.mongo.entities.UserMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.BookMongoMapper;
import com.martinia.indigo.adapters.out.mongo.mapper.NotificationMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.NotificationMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.UserMongoRepository;
import com.martinia.indigo.domain.enums.NotificationEnum;
import com.martinia.indigo.domain.enums.StatusEnum;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Notification;
import com.martinia.indigo.ports.out.mongo.NotificationRepository;

@Component
public class NotificationRepositoryImpl implements NotificationRepository {

	@Autowired
	NotificationMongoRepository notificationMongoRepository;

	@Autowired
	BookMongoRepository bookMongoRepository;

	@Autowired
	UserMongoRepository userMongoRepository;

	@Autowired
	NotificationMongoMapper notificationMongoMapper;

	@Autowired
	BookMongoMapper bookMongoMapper;

	@Override
	public List<Notification> findAllByOrderBySendDateDesc() {
		return notificationMongoMapper
				.entities2Domains(notificationMongoRepository.findAll(Sort.by(Sort.Direction.DESC, "sendDate")));

	}

	@Override
	public List<Notification> findByUserAndType(String user, NotificationEnum type) {
		return notificationMongoMapper.entities2Domains(notificationMongoRepository.findByUser(user));

	}

	@Override
	public List<Notification> findByUserAndStatus(String user, StatusEnum status) {
		return notificationMongoMapper.entities2Domains(notificationMongoRepository.findByUserAndStatus(user, status));

	}

	@Override
	public List<Notification> findByStatus(StatusEnum status) {
		return notificationMongoMapper.entities2Domains(notificationMongoRepository.findByStatus(status));

	}

	@Override
	public List<Notification> findByUserAndReadUserFalse(String user) {
		return notificationMongoMapper.entities2Domains(notificationMongoRepository.findByUserAndReadUserIsFalse(user));

	}

	@Override
	public List<Notification> findByReadAdminFalse() {
		return notificationMongoMapper.entities2Domains(notificationMongoRepository.findByReadAdminIsFalse());
	}

	@Override
	public void save(Notification notification) {
		notificationMongoRepository.save(notificationMongoMapper.domain2Entity(notification));
	}

	@Override
	public Notification findById(String id) {
		return notificationMongoMapper.entity2Domain(notificationMongoRepository.findById(id)
				.get());
	}

	@Override
	public void delete(String id) {
		notificationMongoRepository.delete(notificationMongoRepository.findById(id)
				.get());

	}

	@Override
	public void markAsRead(String id, String user) {
		UserMongoEntity usr = userMongoRepository.findById(user)
				.get();
		NotificationMongoEntity notification = notificationMongoRepository.findById(id)
				.get();
		if (usr.getRole()
				.equals("ADMIN"))
			notification.setReadAdmin(true);
		else
			notification.setReadUser(true);
		notificationMongoRepository.save(notification);
	}

	@Override
	public List<Book> getSentBooks(String user) {
		Map<String, Book> map = new HashMap<>();
		List<NotificationMongoEntity> notifs = notificationMongoRepository.findByUser(user);

		for (NotificationMongoEntity notif : notifs) {
			if (!map.containsKey(notif.getBook())) {
				BookMongoEntity book = bookMongoRepository.findByPath(notif.getBook());
				if (book != null)
					map.put(notif.getBook(), bookMongoMapper.entity2Domain(book));
			}
		}

		return new ArrayList<>(map.values());
	}

}
