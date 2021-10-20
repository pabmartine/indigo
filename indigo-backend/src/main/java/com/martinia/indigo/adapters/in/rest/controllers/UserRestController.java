package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.adapters.in.rest.dtos.UserDto;
import com.martinia.indigo.adapters.in.rest.mappers.UserDtoMapper;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.in.rest.UserService;

@RestController
@RequestMapping("/rest/user")
public class UserRestController {

	@Autowired
	private UserService userService;

	@Autowired
	protected UserDtoMapper userDtoMapper;

	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> get(@RequestParam String username) {
		User user = userService.findByUsername(username);
		UserDto userDto = userDtoMapper.domain2Dto(user);
		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

	@GetMapping(value = "/getById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getById(@RequestParam String id) {
		User user = userService.findById(id);
		UserDto userDto = userDtoMapper.domain2Dto(user);
		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserDto>> getAll() {
		List<User> users = userService.findAll();
		List<UserDto> usersDto = userDtoMapper.domains2Dtos(users);
		return new ResponseEntity<>(usersDto, HttpStatus.OK);
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
	public ResponseEntity<Void> delete(@RequestParam String id) {
		userService.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
