package com.martinia.indigo.metadata.application.libretranslate;

import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.DetectLibreTranslateUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "flags.libretranslate", havingValue="true")
@Transactional
public class DetectLibreTranslateUseCaseImpl implements DetectLibreTranslateUseCase {

	@Resource
	private RestTemplate restTemplate;

	@Value("${metadata.libretranslate.url}")
	private String endpoint;

	@Override
	public String detect(String text) {
		String ret = null;
		try {
			String url = endpoint + "/detect";

			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("q", text);

			Object responseObject = restTemplate.postForObject(url, map, Object.class);
			if (responseObject instanceof List) {
				List<?> list = (List<?>) responseObject;
				if (!list.isEmpty() && list.get(0) instanceof LinkedHashMap) {
					ret = ((LinkedHashMap<?, ?>) list.get(0)).get("language").toString();
				}
			}
		}
		catch (Exception e) {
			log.debug("Language detection is unavailable: {}", e.getMessage());
			com.martinia.indigo.metadata.application.ProviderDiagnostics.record("LIBRETRANSLATE", "Detectar idioma", e);
		}
		return ret;
	}

}
