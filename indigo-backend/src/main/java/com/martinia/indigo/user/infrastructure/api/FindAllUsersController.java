package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.UserDto;
import com.martinia.indigo.adapters.in.rest.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.service.FindAllUsersUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/user")
public class FindAllUsersController {

	@Autowired
	private FindAllUsersUseCase useCase;

	@Autowired
	protected UserDtoMapper mapper;

	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserDto>> getAll() {
		final List<User> users = useCase.findAll();
		final List<UserDto> usersDto = mapper.domains2Dtos(users);
		return new ResponseEntity<>(usersDto, HttpStatus.OK);
	}

}
