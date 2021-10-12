package com.martinia.indigo.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.enums.RolesEnum;
import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.services.indigo.UserService;

@RestController
@RequestMapping("/rest/user")
public class UserRestController {

	@Autowired
	private UserService userService;

	@Autowired
	PasswordEncoder passwordEncoder;

	// TODO MAPPING
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> get(@RequestParam String username) {
		return new ResponseEntity<>(userService.findByUsername(username), HttpStatus.OK);
	}

	// TODO MAPPING
	@GetMapping(value = "/getById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> getById(@RequestParam int id) {
		return new ResponseEntity<>(userService.findById(id)
				.get(), HttpStatus.OK);
	}

	// TODO MAPPING
	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Iterable<User>> getAll() {
		return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@RequestBody User user) {

		User _user = userService.findById(user.getId())
				.get();

		if (!_user.getPassword()
				.equals(user.getPassword()))
			_user.setPassword(passwordEncoder.encode(user.getPassword()));

		_user.setUsername(user.getUsername());
		_user.setLanguage(user.getLanguage());
		_user.setKindle(user.getKindle());

		userService.save(_user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody User user) {
		user.setRole(RolesEnum.USER.name());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userService.save(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO Bajar a servicio?
	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@RequestParam int id) {
		User user = userService.findById(id)
				.get();
		userService.delete(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
