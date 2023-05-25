package com.martinia.indigo.common.configuration.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.ConfigurationDto;
import com.martinia.indigo.adapters.in.rest.mappers.ConfigurationDtoMapper;
import com.martinia.indigo.common.configuration.domain.service.FindConfigurationByKeyUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/config")
public class FindConfigurationByKeyController {

	@Resource
	private FindConfigurationByKeyUseCase useCase;

	@Autowired
	protected ConfigurationDtoMapper mapper;

	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConfigurationDto> get(@RequestParam final String key) {
		ConfigurationDto configurationDto = useCase.findByKey(key).map(conf -> mapper.domain2Dto(conf)).orElse(null);
		return new ResponseEntity<>(configurationDto, HttpStatus.OK);
	}


}
