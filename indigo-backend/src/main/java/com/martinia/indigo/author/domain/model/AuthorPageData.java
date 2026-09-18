package com.martinia.indigo.author.domain.model;

import java.util.List;

public record AuthorPageData(List<Author> items, long total) {
}
