package com.martinia.indigo.controllers.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.services.calibre.PageService;

@RestController
@RequestMapping("/rest/page")
public class PageRestController {

	@Autowired
	private PageService pageService;

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Integer> findPagesByBookId(@RequestParam int id) {

		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("pages", pageService.findPagesByBookId(id));

		return map;
	}

}
