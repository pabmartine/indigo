package com.martinia.indigo.controllers.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.services.calibre.CommentService;

@RestController
@RequestMapping("/rest/comment")
public class CommentRestController {

	@Autowired
	private CommentService commentService;

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> findTextByBookId(@RequestParam int id) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("comment", commentService.findTextByBookId(id));

		return map;
	}

}
