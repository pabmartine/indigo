package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.metadata.domain.service.StopMetadataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/metadata")
public class StopMetadataController {

	@Resource
	private StopMetadataUseCase useCase;

	@GetMapping(value = "/stop")
	public ResponseEntity<Void> stop() {
		useCase.stop();
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
