package com.martinia.indigo.adapters.out.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.ports.out.metadata.LibreTranslateService;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibreTranslateServiceImpl implements LibreTranslateService {

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

			List list = (ArrayList)restTemplate.postForObject(url, map, Object.class);
			ret = ((LinkedHashMap) list.get(0)).get("language").toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public String translate(String text, String target) {
		String ret = null;
		try {
			String url = URL + "/translate";

			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("q", text);
			map.add("source", "auto");
			map.add("target", target);
			map.add("format", "text");

			Map mapRet = (LinkedHashMap)restTemplate.postForObject(url, map, Object.class);
			ret = mapRet.get("translatedText").toString();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
