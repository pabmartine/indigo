package com.martinia.indigo.repository.calibre;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CustomTagRepositoryImpl implements CustomTagRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Object[]> getNumBooksByTag(String sort, String order) {

		String select = "select t.id, t.name, count(l.book) as total from BooksTagsLink l, Tag t where l.tag = t.id group by l.tag order by "
				+ sort + " " + order;

		return entityManager.createQuery(select).getResultList();

	}

}
