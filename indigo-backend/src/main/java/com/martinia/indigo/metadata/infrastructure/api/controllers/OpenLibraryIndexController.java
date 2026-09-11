package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.application.openlibrary.OpenLibraryIndexManager;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexJobMongoEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metadata/openlibrary/index")
@Tag(name = "Open Library index")
public class OpenLibraryIndexController {
	@Resource
	private OpenLibraryIndexManager indexManager;

	@GetMapping
	@Operation(summary = "Returns the durable Open Library index job status")
	public OpenLibraryIndexJobMongoEntity status() {
		return indexManager.status();
	}

	@PostMapping("/start")
	@Operation(summary = "Starts a new Open Library index build")
	public ResponseEntity<OpenLibraryIndexJobMongoEntity> start() {
		return ResponseEntity.accepted().body(indexManager.start());
	}

	@PostMapping("/resume")
	@Operation(summary = "Resumes a failed or cancelled Open Library index build")
	public ResponseEntity<OpenLibraryIndexJobMongoEntity> resume() {
		return ResponseEntity.accepted().body(indexManager.resume());
	}

	@PostMapping("/cancel")
	@Operation(summary = "Requests cancellation of the active Open Library index build")
	public ResponseEntity<OpenLibraryIndexJobMongoEntity> cancel() {
		return ResponseEntity.accepted().body(indexManager.cancel());
	}
}
