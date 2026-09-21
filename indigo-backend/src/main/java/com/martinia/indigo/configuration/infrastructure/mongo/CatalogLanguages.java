package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

import java.util.List;

/** Equality-filterable languages, derived from the existing positive book counters. */
@Component
public class CatalogLanguages implements BeforeSaveCallback<Object> {
	public static List<String> from(Document document) {
		Document counts = document.get("numBooks", Document.class);
		Document languages = counts == null ? null : counts.get("languages", Document.class);
		if (languages == null) return List.of();
		return LanguageCodeUtils.normalizeAll(languages.entrySet().stream()
				.filter(entry -> entry.getValue() instanceof Number count && count.longValue() > 0)
				.map(java.util.Map.Entry::getKey).toList());
	}

	@Override
	public Object onBeforeSave(Object entity, Document document, String collection) {
		if (entity instanceof AuthorMongoEntity || entity instanceof TagMongoEntity) {
			document.put("catalogLanguages", from(document));
		}
		return entity;
	}
}
