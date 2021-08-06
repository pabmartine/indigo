package com.martinia.indigo.repository.calibre;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CustomSerieRepositoryImpl implements CustomSerieRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Object[]> getNumBooksBySerie(int page, int size, String sort, String order) {

		String select = "select s.id, s.name, s.sort, count(l.book) as total from BooksSeriesLink l, Serie s where l.series = s.id group by l.series  order by "
				+ sort + " " + order;

		return entityManager.createQuery(select).setFirstResult(page * size).setMaxResults(size).getResultList();

	}

}
