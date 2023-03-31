package com.martinia.indigo.ports.out.metadata;

import com.martinia.indigo.domain.model.inner.Review;

import java.util.List;

public interface LibreTranslateService {

	String detect(String text);

	String translate(String text, String target);

}
