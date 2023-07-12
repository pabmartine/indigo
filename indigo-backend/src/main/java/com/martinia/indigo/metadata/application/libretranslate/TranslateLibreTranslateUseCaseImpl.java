package com.martinia.indigo.metadata.application.libretranslate;

import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.TranslateLibreTranslateUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "flags.libretranslate", havingValue="true")
public class TranslateLibreTranslateUseCaseImpl implements TranslateLibreTranslateUseCase {

	@Resource
	private RestTemplate restTemplate;

	@Value("${metadata.libretranslate.url}")
	private String endpoint;

	@Override
	public String translate(String text, String target) {
		String ret = null;
		try {
			String url = endpoint + "/translate";

			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("q", text);
			map.add("source", "auto");
			map.add("target", target);
			map.add("format", "text");

			Map mapRet = (LinkedHashMap) restTemplate.postForObject(url, map, Object.class);
			ret = mapRet.get("translatedText").toString();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
