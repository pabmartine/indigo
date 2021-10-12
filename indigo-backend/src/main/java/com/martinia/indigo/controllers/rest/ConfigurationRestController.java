package com.martinia.indigo.controllers.rest;

import java.util.List;
import java.util.Optional;

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

import com.martinia.indigo.model.indigo.Configuration;
import com.martinia.indigo.services.indigo.ConfigurationService;

@RestController
@RequestMapping("/rest/config")
public class ConfigurationRestController {

	@Autowired
	private ConfigurationService configurationService;
	
	//TODO MAPPING
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Optional<Configuration>> get(@RequestParam String key) {
		return new ResponseEntity<>(configurationService.findById(key), HttpStatus.OK);

	}
	
	// TODO Bajar a servicio?
	//TODO MAPPING
	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> save(@RequestBody List<Configuration> configurations) {

		for (Configuration configuration : configurations) {
			Optional<Configuration> optional = configurationService.findById(configuration.getKey());

			if (!optional.isPresent() || !optional.get()
					.getValue()
					.equals(configuration.getValue())) {
				configurationService.save(configuration);
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
