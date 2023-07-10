package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.user.infrastructure.model.UserDto;
import com.martinia.indigo.user.infrastructure.mapper.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindAllUsersUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class FindAllUsersController {

	@Resource
	private FindAllUsersUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserDto>> getAll() {
		final List<User> users = useCase.findAll();
		final List<UserDto> usersDto = mapper.domains2Dtos(users);
		return new ResponseEntity<>(usersDto, HttpStatus.OK);
	}

}
