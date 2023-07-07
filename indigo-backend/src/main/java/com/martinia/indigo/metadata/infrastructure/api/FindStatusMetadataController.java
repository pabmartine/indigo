package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.metadata.domain.service.FindStatusMetadataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/rest/metadata")
public class FindStatusMetadataController {

	@Resource
	private FindStatusMetadataUseCase useCase;

	@GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getStatus() {
		return new ResponseEntity<>(useCase.getStatus(), HttpStatus.OK);
	}

}
