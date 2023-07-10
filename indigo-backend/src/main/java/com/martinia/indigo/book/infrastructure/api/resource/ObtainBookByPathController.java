package com.martinia.indigo.book.infrastructure.api.resource;

import com.martinia.indigo.book.domain.ports.usecases.resource.ObtainBookByPathUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/book")
public class ObtainBookByPathController {

	@Resource
	private ObtainBookByPathUseCase useCase;

	@GetMapping(value = "/epub")
	public ResponseEntity<org.springframework.core.io.Resource> getEpub(@RequestParam String path) throws IOException {

		org.springframework.core.io.Resource epub = useCase.getEpub(path);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(epub.getFile().toPath()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + epub.getFilename() + "\"").body(epub);
	}

}
