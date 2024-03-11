package com.martinia.indigo.file.infrastructure.api.controllers;

import com.martinia.indigo.file.domain.ports.usecases.FindEpubFilesUploadPathUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
public class FindEpubFilesUploadPathController {

	@Resource
	private FindEpubFilesUploadPathUseCase useCase;

	@GetMapping(value = "/path", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> findPath() {
		Map<String, String> map = new HashMap<>();
		final String path = useCase.findPath();
		map.put("path", path);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
