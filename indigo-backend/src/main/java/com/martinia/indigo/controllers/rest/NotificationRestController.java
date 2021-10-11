package com.martinia.indigo.controllers.rest;

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
import com.martinia.indigo.services.indigo.NotificationService;
import com.martinia.indigo.services.indigo.UserService;

@RestController
@RequestMapping("/rest/notification")
public class NotificationRestController {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserService userService;
	//TODO MAPPING
	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAll() {
		return notificationService.findAllByOrderByIdDesc();
	}
	//TODO MAPPING
	@GetMapping(value = "/not_read", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAllNotRead() {
		return notificationService.findNotReadAdmin();
	}
	//TODO MAPPING
	@GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAllByUser(@RequestParam int user) {
		return notificationService.findByUser(user);
	}
	//TODO MAPPING
	@GetMapping(value = "/not_read_user", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<Notification> findAllNotReadByUser(@RequestParam int user) {
		return notificationService.findNotReadUser(user);
	}

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void save(@RequestBody Notification notification) {
		notificationService.save(notification);
	}

	@GetMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
	public void markAsRead(@RequestParam int id, @RequestParam int user) {
		User usr = userService.findById(user)
				.get();
		Notification notification = notificationService.findById(id)
				.get();
		if (usr.getRole()
				.equals("ADMIN"))
			notification.setReadByAdmin(true);
		else
			notification.setReadByUser(true);
		notificationService.save(notification);
	}

	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public void delete(@RequestParam int id) {
		Notification notification = notificationService.findById(id)
				.get();
		notificationService.delete(notification);
	}

}
