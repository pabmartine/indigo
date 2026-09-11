package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.application.MetadataActivityService;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.*;
import com.martinia.indigo.metadata.domain.model.commands.*;
import jakarta.annotation.Resource;
import java.util.List;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/metadata/activity")
public class MetadataActivityController {
    @Resource private MetadataActivityService activity;
    @Resource private CommandBus commands;
    @GetMapping public List<Document> items() { return activity.items(); }
    @GetMapping("/history") public List<Document> history() { return activity.history(); }
    @PostMapping("/undo/{id}") public void undo(@PathVariable String id) { activity.undo(id); }
    @PostMapping("/lock/{type}/{id}") public void lock(@PathVariable String type, @PathVariable String id,
            @RequestParam boolean locked) { activity.lock(type, id, locked); }
    @PostMapping("/retry/{key}") public MetadataItemResult retry(@PathVariable String key) {
        Document item = activity.item(key);
        if (!"ERROR".equals(item.getString("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only failed items can be retried");
        String id = item.getString("entityId");
        String lang = item.getString("lang") == null ? "es" : item.getString("lang");
        return switch (item.getString("type")) {
            case "BOOKS" -> commands.executeAndWait(FindBookMetadataCommand.builder().bookId(id)
                    .mergePolicy(MetadataMergePolicy.FILL_MISSING).dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE).build());
            case "AUTHORS" -> commands.executeAndWait(FindAuthorMetadataCommand.builder().authorId(id).lang(lang).override(true).build());
            case "REVIEWS" -> commands.executeAndWait(FindReviewMetadataCommand.builder().bookId(id).lang(lang).override(true).build());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        };
    }
}
