package com.martinia.indigo.serie.infrastructure.api.controllers;

import com.martinia.indigo.serie.domain.ports.usecases.FindNumBooksBySerieUseCase;
import com.martinia.indigo.serie.domain.model.SeriePageData;
import com.martinia.indigo.serie.infrastructure.api.model.SeriePageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/serie")
@Tag(name = "Series", description = "API for managing book series")
public class FindNumBooksBySerieController {

	@Resource
	private FindNumBooksBySerieUseCase useCase;

	@Operation(summary = "Get number of books by series",
			description = "Retrieves a list of series with the count of books in each, with pagination and sorting options.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved series book counts",
							content = @Content(mediaType = "application/json",
									schema = @Schema(type = "array", implementation = Map.class, example = "[{\"name\": \"Series A\", \"numBooks\": \"5\"}]" )))
			})
	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, String>>> getNumBooksBySerie(
			@Parameter(description = "List of languages to filter series by (e.g., 'en', 'es')", example = "[\"en\"]") @RequestParam List<String> languages,
			@Parameter(description = "Page number for pagination (0-indexed)", example = "0") @RequestParam int page,
			@Parameter(description = "Number of series per page", example = "10") @RequestParam int size,
			@Parameter(description = "Field to sort by (e.g., 'name')", example = "name") @RequestParam String sort,
			@Parameter(description = "Sort order (asc or desc)", example = "asc") @RequestParam String order) {

		Map<String, Long> data = useCase.getNumBooksBySerie(languages, page, size, sort, order);

		List<Map<String, String>> ret = new ArrayList<>();
		for (String key : data.keySet()) {
			Map<String, String> map = new HashMap<>();
			map.put("name", key);
			map.put("numBooks", data.get(key).toString());
			ret.add(map);
		}

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@Operation(summary = "Get paginated series data",
			description = "Retrieves a page of series together with the total number of available series.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Successfully retrieved paginated series data",
							content = @Content(mediaType = "application/json",
									schema = @Schema(implementation = SeriePageDto.class)))
			})
	@GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SeriePageDto> getSeriesPage(
			@Parameter(description = "List of languages to filter series by (e.g., 'en', 'es')", example = "[\"en\"]") @RequestParam List<String> languages,
			@Parameter(description = "Page number for pagination (0-indexed)", example = "0") @RequestParam int page,
			@Parameter(description = "Number of series per page", example = "10") @RequestParam int size,
			@Parameter(description = "Field to sort by (e.g., 'name')", example = "name") @RequestParam String sort,
			@Parameter(description = "Sort order (asc or desc)", example = "asc") @RequestParam String order) {

		SeriePageData pageData = useCase.getSeriesPage(languages, page, size, sort, order);
		List<Map<String, String>> items = new ArrayList<>();

		for (String key : pageData.items().keySet()) {
			Map<String, String> map = new HashMap<>();
			map.put("name", key);
			map.put("numBooks", pageData.items().get(key).toString());
			items.add(map);
		}

		return new ResponseEntity<>(new SeriePageDto(items, pageData.total(), page, size), HttpStatus.OK);
	}

}
