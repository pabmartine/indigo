package com.martinia.indigo.repository.calibre;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CustomAuthorRepositoryImpl implements CustomAuthorRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Object[]> getNumBooksByAuthor(int page, int size, String sort, String order) {

		String select = "select a.id, a.sort, count(l.book) as total from BooksAuthorsLink l, Author a where l.author = a.id group by l.author  order by "
				+ sort + " " + order;

		return entityManager.createQuery(select).setFirstResult(page * size).setMaxResults(size).getResultList();

	}

}
