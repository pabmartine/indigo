package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.SetTagImageUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/tag")
public class SetTagImageController {

	@Resource
	private SetTagImageUseCase useCase;

	@GetMapping("/image")
	public ResponseEntity<Void> setImage(@RequestParam final String source, @RequestParam final String image) {
		useCase.setImage(source, image);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
