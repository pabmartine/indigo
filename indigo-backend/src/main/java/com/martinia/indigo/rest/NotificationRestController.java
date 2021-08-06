package com.martinia.indigo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.model.indigo.Notification;
import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.repository.indigo.NotificationRepository;
import com.martinia.indigo.repository.indigo.UserRepository;

@RestController
@RequestMapping("/rest/notification")
public class NotificationRestController {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAll() {
		return notificationRepository.findAllByOrderByIdDesc();
	}

	@GetMapping(value = "/not_read", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAllNotRead() {
		return notificationRepository.findNotReadAdmin();
	}

	@GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAllByUser(@RequestParam int user) {
		return notificationRepository.findByUser(user);
	}

	@GetMapping(value = "/not_read_user", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAllNotReadByUser(@RequestParam int user) {
		return notificationRepository.findNotReadUser(user);
	}

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void save(@RequestBody Notification notification) {
		notificationRepository.save(notification);
	}

	@GetMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
	public void markAsRead(@RequestParam int id, @RequestParam int user) {
		User usr = userRepository.findById(user).get();
		Notification notification = notificationRepository.findById(id).get();
		if (usr.getRole().equals("ADMIN"))
			notification.setReadByAdmin(true);
		else
			notification.setReadByUser(true);
		notificationRepository.save(notification);
	}

	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public void delete(@RequestParam int id) {
		Notification notification = notificationRepository.findById(id).get();
		notificationRepository.delete(notification);
	}

}
