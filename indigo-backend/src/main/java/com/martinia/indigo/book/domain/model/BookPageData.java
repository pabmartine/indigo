package com.martinia.indigo.book.domain.model;

import java.util.List;

public record BookPageData(List<Book> items, long total) {
}
