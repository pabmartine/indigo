package com.martinia.indigo.configuration.infrastructure.api.controllers;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.domain.ports.usecases.SaveConfigurationsUseCase;
import com.martinia.indigo.configuration.infrastructure.api.mappers.ConfigurationDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/config")
public class SaveConfigurationsController {

	@Resource
	private SaveConfigurationsUseCase useCase;

	@Autowired
	protected ConfigurationDtoMapper mapper;

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody final List<Configuration> configurations) {
		useCase.save(configurations);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
