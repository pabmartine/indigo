package com.martinia.indigo.services.calibre.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.martinia.indigo.repository.calibre.SerieRepository;
import com.martinia.indigo.services.calibre.SerieService;

@Service
public class SerieServiceImpl implements SerieService {

	@Autowired
	SerieRepository serieRepository;

	@Override
	public String getSerieByBook(int id) {
		return serieRepository.getSerieByBook(id);
	}

	@Override
	public List<String> getBookPathSerieById(int id, Pageable pageable) {
		return serieRepository.getBookPathSerieById(id, pageable);
	}

	@Override
	public long count() {
		return serieRepository.count();
	}

	@Override
	public List<Object[]> getNumBooksBySerie(int page, int size, String sort, String order) {
		return serieRepository.getNumBooksBySerie(page, size, sort, order);
	}

}
