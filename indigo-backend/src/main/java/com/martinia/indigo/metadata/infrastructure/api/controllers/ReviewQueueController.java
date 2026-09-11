package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.application.reviews.ReviewQueueService;
import jakarta.annotation.Resource;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/metadata/review-queue")
public class ReviewQueueController {
    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> invalidState(org.springframework.web.server.ResponseStatusException error) {
        return org.springframework.http.ResponseEntity.status(error.getStatusCode())
                .body(java.util.Map.of("message", error.getReason() == null ? "Operación no válida" : error.getReason()));
    }
    @Resource private ReviewQueueService queue;
    @GetMapping public Document status() { return queue.status(); }
    @PostMapping("/start") public Document start(@RequestParam boolean all, @RequestParam(defaultValue="es") String lang,
            @RequestParam(defaultValue="false") boolean replace) { return queue.start(all, lang, replace); }
    @PostMapping("/settings") public void settings(@RequestParam int amazon, @RequestParam int goodreads) { queue.configure(amazon, goodreads); }
    @PostMapping("/{action}") public Document control(@PathVariable String action) { return queue.control(action); }
}
