package com.martinia.indigo.controllers.rest;

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

import com.martinia.indigo.dto.UserDto;
import com.martinia.indigo.mappers.UserDtoMapper;
import com.martinia.indigo.model.indigo.User;
import com.martinia.indigo.services.indigo.UserService;

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
		UserDto userDto = userDtoMapper.userToUserDto(user);
		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

	@GetMapping(value = "/getById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getById(@RequestParam int id) {
		User user = userService.findById(id)
				.orElse(null);
		UserDto userDto = userDtoMapper.userToUserDto(user);
		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserDto>> getAll() {
		List<User> users = userService.findAll();
		List<UserDto> usersDto = userDtoMapper.usersToUserDtos(users);
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
	public ResponseEntity<Void> delete(@RequestParam int id) {
		userService.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
