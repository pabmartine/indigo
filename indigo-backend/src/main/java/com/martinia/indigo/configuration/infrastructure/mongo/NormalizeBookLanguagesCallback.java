package com.martinia.indigo.configuration.infrastructure.mongo;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.util.LanguageCodeUtils;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class NormalizeBookLanguagesCallback implements BeforeConvertCallback<Object> {
	@Override
	public Object onBeforeConvert(Object entity, String collection) {
		if (entity instanceof BookMongoEntity book && book.getLanguages() != null) {
			book.setLanguages(LanguageCodeUtils.normalizeAll(book.getLanguages()));
		}
		if (entity instanceof UserMongoEntity user && user.getLanguageBooks() != null) {
			user.setLanguageBooks(LanguageCodeUtils.normalizeAll(user.getLanguageBooks()));
		}
		return entity;
	}
}
