package com.martinia.indigo.serie.domain.model;

import java.util.Map;

public record SeriePageData(Map<String, Long> items, long total) {
}
