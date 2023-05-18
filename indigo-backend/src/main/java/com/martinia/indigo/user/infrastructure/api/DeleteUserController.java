package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.service.DeleteUserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/user")
public class DeleteUserController {

	@Autowired
	private DeleteUserUseCase useCase;

	@Autowired
	protected UserDtoMapper mapper;

	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> delete(@RequestParam final String id) {
		useCase.delete(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
