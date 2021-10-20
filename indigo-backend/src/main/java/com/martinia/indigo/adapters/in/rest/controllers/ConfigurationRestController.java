package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.adapters.in.rest.dtos.ConfigurationDto;
import com.martinia.indigo.adapters.in.rest.mappers.ConfigurationDtoMapper;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.in.rest.ConfigurationService;

@RestController
@RequestMapping("/rest/config")
public class ConfigurationRestController {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	protected ConfigurationDtoMapper configurationDtoMapper;

	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConfigurationDto> get(@RequestParam String key) {
		Configuration configuration = configurationService.findByKey(key);
		ConfigurationDto configurationDto = configurationDtoMapper.domain2Dto(configuration);
		return new ResponseEntity<>(configurationDto, HttpStatus.OK);

	}

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody List<Configuration> configurations) {
		configurationService.save(configurations);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
