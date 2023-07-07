package com.martinia.indigo.metadata.infrastructure.api;

import com.martinia.indigo.author.infrastructure.model.AuthorDto;
import com.martinia.indigo.author.infrastructure.mapper.AuthorDtoMapper;
import com.martinia.indigo.metadata.domain.service.RefreshAuthorMetadataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rest/metadata")
public class RefreshAuthorMetadataController {

	@Resource
	private RefreshAuthorMetadataUseCase useCase;

	@Resource
	protected AuthorDtoMapper mapper;

	@GetMapping(value = "/author", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthorDto> refreshAuthor(@RequestParam String lang, @RequestParam String author) {
		AuthorDto authorDto = useCase.findAuthorMetadata(author, lang).map(_author -> mapper.domain2Dto(_author)).orElse(null);
		return new ResponseEntity<>(authorDto, HttpStatus.OK);
	}

}
