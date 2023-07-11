package com.martinia.indigo.user.infrastructure.api.controllers;

import com.martinia.indigo.user.infrastructure.api.model.UserDto;
import com.martinia.indigo.user.infrastructure.api.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class FindUserByUsernameController {

	@Resource
	private FindUserByUsernameUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> get(@RequestParam final String username) {
		final Optional<User> user = useCase.findByUsername(username);
		final UserDto userDto = user.map(usr -> mapper.domain2Dto(usr)).orElse(null);
		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

}
