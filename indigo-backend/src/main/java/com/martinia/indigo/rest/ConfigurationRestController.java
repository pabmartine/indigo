package com.martinia.indigo.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.model.indigo.Configuration;
import com.martinia.indigo.repository.indigo.ConfigurationRepository;

@RestController
@RequestMapping("/rest/config")
public class ConfigurationRestController {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public Optional<Configuration> get(@RequestParam String key) {
		return configurationRepository.findById(key);
	}

	@PutMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void save(@RequestBody List<Configuration> configurations) {

		for (Configuration configuration : configurations) {
			Optional<Configuration> optional = configurationRepository.findById(configuration.getKey());
	
			if (!optional.isPresent() || !optional.get().getValue().equals(configuration.getValue())) {
				configurationRepository.save(configuration);
			}
		}
	}

}
