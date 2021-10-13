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


	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@RequestBody User user) {		
		userService.update(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody User user) {
		userService.save(user, true);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@RequestParam int id) {
		userService.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
