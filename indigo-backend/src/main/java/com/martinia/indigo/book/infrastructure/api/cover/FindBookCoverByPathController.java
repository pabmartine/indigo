package com.martinia.indigo.book.infrastructure.api.cover;

import com.martinia.indigo.adapters.in.rest.mappers.BookDtoMapper;
import com.martinia.indigo.book.domain.service.cover.FindBookCoverByPathUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rest/book")
public class FindBookCoverByPathController {

	@Resource
	private FindBookCoverByPathUseCase useCase;

	@Resource
	private BookDtoMapper mapper;

	@GetMapping(value = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> getImage(@RequestParam String path) {
		Map<String, String> map = null;

		Optional<String> image = useCase.getImage(path);

		if (image.isPresent()) {
			map = new HashMap<String, String>();
			map.put("image", image.get());
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}
}
