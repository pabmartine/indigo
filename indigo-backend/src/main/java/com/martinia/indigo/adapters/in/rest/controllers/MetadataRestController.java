package com.martinia.indigo.adapters.in.rest.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.ports.in.rest.MetadataService;

@RestController
@RequestMapping("/rest/metadata")
public class MetadataRestController {

	@Autowired
	private MetadataService metadataService;

	@GetMapping(value = "/full")
	public ResponseEntity<Void> initialLoad(@RequestParam String lang) {
		metadataService.initialLoad(lang);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/partial")
	public ResponseEntity<Void> getAllAuthorsData(@RequestParam String lang) {
		metadataService.noFilledMetadata(lang);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/stop")
	public ResponseEntity<Void> stop() {
		metadataService.stop();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getStatus() {
		Map<String, Object> ret = null;
		ret = metadataService.getStatus();
		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

}
