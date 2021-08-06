package com.martinia.indigo.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.martinia.indigo.repository.calibre.SerieRepository;

@RestController
@RequestMapping("/rest/serie")
public class SerieRestController {

	@Autowired
	private SerieRepository serieRepository;

	@Value("${book.library.path}")
	private String libraryPath;

	@GetMapping("/count")
	public long getTotal() {
		return serieRepository.count();
	}

	@GetMapping(value = "/numbooks", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> getNumBooksBySerie(@RequestParam int page, @RequestParam int size,
			@RequestParam String sort, @RequestParam String order) {
		List<Object[]> data = serieRepository.getNumBooksBySerie(page, size, sort, order);

		List<Map<String, String>> ret = new ArrayList<>(data.size());
		for (Object[] o : data) {
			Map<String, String> map = new HashMap<>();
			map.put("id", o[0].toString());
			map.put("name", o[1].toString());
			map.put("sort", o[2].toString());
			map.put("total", o[3].toString());
			ret.add(map);
		}
		return ret;
	}

	@GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> findPagesByBookId(@RequestParam int id) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("serie", serieRepository.getSerieByBook(id));

		return map;
	}

	@GetMapping(value = "/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> getCover(@RequestParam int id) throws IOException {
		Map<String, String> map = null;

		if (!libraryPath.endsWith(File.separator))
			libraryPath += File.separator;

		String path = serieRepository.getBookPathSerieById(id, PageRequest.of(0, 1, Sort.by("seriesIndex"))).get(0);

		String basePath = libraryPath + path + "/cover.jpg";

		File file = new File(basePath);

		String image = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));

		map = new HashMap<String, String>();
		map.put("image", image);

		return map;
	}

}
