package com.martinia.indigo.metadata.application.libretranslate;

import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.DetectLibreTranslateUseCase;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class DetectLibreTranslateUseCaseImpl implements DetectLibreTranslateUseCase {

	@Resource
	private RestTemplate restTemplate;

	private static String URL = "http://192.168.1.40:6000";

	@Override
	public String detect(String text) {
		String ret = null;
		try {
			String url = URL + "/detect";

			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("q", text);

			List list = (ArrayList) restTemplate.postForObject(url, map, Object.class);
			ret = ((LinkedHashMap) list.get(0)).get("language").toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

}
