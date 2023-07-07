package com.martinia.indigo.tag.infrastructure.api;

import com.martinia.indigo.tag.domain.service.RenameTagUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/tag")
public class RenameTagController {

	@Resource
	private RenameTagUseCase useCase;

	@GetMapping("/rename")
	public ResponseEntity<Void> rename(@RequestParam final String source, @RequestParam final String target) {
		useCase.rename(source, target);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
