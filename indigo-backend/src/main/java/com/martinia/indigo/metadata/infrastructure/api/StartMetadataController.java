package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/metadata")
public class StartMetadataController {

	@Resource
	private StartMetadataUseCase useCase;

	@GetMapping(value = "/start")
	public ResponseEntity<Void> initialLoad(@RequestParam String lang, @RequestParam String type, @RequestParam String entity) {
		useCase.start(lang, type, entity);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
