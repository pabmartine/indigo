package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.file.application.PendingImportService;
import jakarta.annotation.Resource;
import java.util.List;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

/** Uses the existing ADMIN-only metadata API namespace. */
@RestController
@RequestMapping("/api/metadata/pending-imports")
public class PendingImportController {
    @Resource private PendingImportService imports;
    @GetMapping public List<Document> pending() { return imports.pending(); }
    @PostMapping("/{id}/retry") public Document retry(@PathVariable String id) { return imports.retry(id); }
}
