package com.martinia.indigo.ports.out.metadata;

public interface LibreTranslateService {

	String detect(String text);

	String translate(String text, String target);

}
