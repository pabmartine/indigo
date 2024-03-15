package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.UploadEpubFilesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/file")
public class UploadEpubFilesController {

	@Resource
	private UploadEpubFilesUseCase useCase;

	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> upload(@RequestParam Long number) {
		useCase.upload(number);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
