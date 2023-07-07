package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.user.infrastructure.mapper.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.service.UpdateUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/user")
public class UpdateUserController {

	@Resource
	private UpdateUserUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> update(@RequestBody final User user) {
		useCase.update(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
