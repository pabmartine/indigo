package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@RestController
@RequestMapping("/api/tag")
public class MergeTagController {

	@Resource
	private MergeTagUseCase useCase;

	@Transactional
	@GetMapping("/merge")
	public ResponseEntity<Void> merge(@RequestParam final String source, @RequestParam final String target) {
		useCase.merge(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
