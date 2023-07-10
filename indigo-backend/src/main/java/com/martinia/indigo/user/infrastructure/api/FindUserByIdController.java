package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.user.infrastructure.model.UserDto;
import com.martinia.indigo.user.infrastructure.mapper.UserDtoMapper;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByIdUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
public class FindUserByIdController {

	@Resource
	private FindUserByIdUseCase useCase;

	@Resource
	private UserDtoMapper mapper;

	@GetMapping(value = "/getById", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getById(@RequestParam final String id) {
		final UserDto userDto = useCase.findById(id).map(user -> mapper.domain2Dto(user)).orElse(null);
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(userDto, headers, HttpStatus.OK);
	}

}
