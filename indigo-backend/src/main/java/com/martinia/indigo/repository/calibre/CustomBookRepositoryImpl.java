package com.martinia.indigo.repository.calibre;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.martinia.indigo.dto.Search;
import com.martinia.indigo.model.calibre.Book;
import com.martinia.indigo.model.calibre.Tag;

public class CustomBookRepositoryImpl implements CustomBookRepository {

	@PersistenceContext
	private EntityManager entityManager;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	
	public long count(Search search) {

		String select = "select count(b.id) from Book b";
		
		if (search==null) {
			search = new Search();
		}

		if (null != (search.getMin()) || null != (search.getMax())) {
			select += ", Pages p";
		}

		if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
			select += ", BooksTagsLink t";
		}

		if (StringUtils.isNoneEmpty(search.getSerie())) {
			select += ", BooksSeriesLink sl";
			select += ", Serie s";
		}

		if (!search.isEmpty()) {

			select += " where (1=1)";
			
			if (StringUtils.isNoneEmpty(search.getPath())) {
				
				if (select.endsWith(")"))
					select += " and";
				
				String path = StringUtils.stripAccents(search.getPath());
				String[] terms = path.split(" ");
				select += " (";
				for (String term : terms) {
					select += "b.path like '%" + term + "%' and ";
				}
				if (select.endsWith(" and "))
					select = select.substring(0, select.lastIndexOf(" and "));
				select += ")";

			}

			if (StringUtils.isNoneEmpty(search.getTitle())) {
				
				if (select.endsWith(")"))
					select += " and";
				
				String title = (search.getTitle());
				String[] terms = title.split(" ");
				select += " (";
				for (String term : terms) {
					select += "b.title like '%" + term + "%' and ";
				}
				if (select.endsWith(" and "))
					select = select.substring(0, select.lastIndexOf(" and "));
				select += ")";
			}

			if (StringUtils.isNoneEmpty(search.getAuthor())) {

				if (select.endsWith(")"))
					select += " and";

				String author = (search.getAuthor());
				String[] terms = author.split(" ");
				select += " (";
				for (String term : terms) {
					select += "b.authorSort like '%" + term + "%' and ";
				}
				if (select.endsWith(" and "))
					select = select.substring(0, select.lastIndexOf(" and "));
				select += ")";
			}

			if (null != (search.getIni())) {
				if (select.endsWith(")"))
					select += " and";
				select += " ( b.pubDate >= :ini )";
			}

			if (null != (search.getEnd())) {
				if (select.endsWith(")"))
					select += " and";
				select += " ( b.pubDate <= :end )";
			}

			if (null != (search.getMin()) || null != (search.getMax())) {

				if (select.endsWith(")"))
					select += " and";
				select += " ( p.book = b.id )";

				if (null != (search.getMin())) {
					if (select.endsWith(")"))
						select += " and";
					select += " ( p.value >= :min )";
				}

				if (null != (search.getMax())) {
					if (select.endsWith(")"))
						select += " and";
					select += " ( p.value <= :max )";
				}
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				if (select.endsWith(")"))
					select += " and";
				select += " ( t.book = b.id and t.tag in :tags )";
			}

			if (StringUtils.isNoneEmpty(search.getSerie())) {
				if (select.endsWith(")"))
					select += " and";
				select += " (b.id = sl.book and sl.series = s.id and s.name like :serie )";
			}

		}

		Query query = entityManager.createQuery(select);
		if (search.getIni() != null) {
			query.setParameter("ini", sdf.format(search.getIni()));
		}
		if (search.getEnd() != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(search.getEnd());
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			query.setParameter("end", sdf.format(c.getTime()));
		}
		if (search.getMin() != null) {
			query.setParameter("min", search.getMin().intValue());
		}
		if (search.getMax() != null) {
			query.setParameter("max", search.getMax().intValue());
		}
		if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
			List<Integer> tags = new ArrayList<>(search.getSelectedTags().size());
			for (Tag tag : search.getSelectedTags()) {
				tags.add(tag.getId());
			}
			query.setParameter("tags", tags);
		}

		if (StringUtils.isNoneEmpty(search.getSerie())) {
			query.setParameter("serie", "%" + search.getSerie() + "%");
		}
		return (Long) query.getSingleResult();
	}

	public List<Book> findAll(Search search, int page, int size, String sort, String order) {

		String select = "select b from Book b";
		
		if (search==null) {
			search = new Search();
		}

		if (null != (search.getMin()) || null != (search.getMax())) {
			select += ", Pages p";
		}

		if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
			select += ", BooksTagsLink t";
		}

		if (StringUtils.isNoneEmpty(search.getSerie())) {
			select += ", BooksSeriesLink sl";
			select += ", Serie s";
		}

		if (!search.isEmpty()) {

			select += " where (1=1)";
			
			if (StringUtils.isNoneEmpty(search.getPath())) {
				
				if (select.endsWith(")"))
					select += " and";
				
				String path = StringUtils.stripAccents(search.getPath());
				String[] terms = path.split(" ");
				select += " (";
				for (String term : terms) {
					select += "b.path like '%" + term + "%' and ";
				}
				if (select.endsWith(" and "))
					select = select.substring(0, select.lastIndexOf(" and "));
				select += ")";

			}

			if (StringUtils.isNoneEmpty(search.getTitle())) {
				
				if (select.endsWith(")"))
					select += " and";
				
				String title = (search.getTitle());
				String[] terms = title.split(" ");
				select += " (";
				for (String term : terms) {
					select += "b.title like '%" + term + "%' and ";
				}
				if (select.endsWith(" and "))
					select = select.substring(0, select.lastIndexOf(" and "));
				select += ")";
			}

			if (StringUtils.isNoneEmpty(search.getAuthor())) {

				if (select.endsWith(")"))
					select += " and";

				String author = (search.getAuthor());
				String[] terms = author.split(" ");
				select += " (";
				for (String term : terms) {
					select += "b.authorSort like '%" + term + "%' and ";
				}
				if (select.endsWith(" and "))
					select = select.substring(0, select.lastIndexOf(" and "));
				select += ")";
			}

			if (null != (search.getIni())) {
				if (select.endsWith(")"))
					select += " and";
				select += " ( b.pubDate >= :ini )";
			}

			if (null != (search.getEnd())) {
				if (select.endsWith(")"))
					select += " and";
				select += " ( b.pubDate <= :end )";
			}

			if (null != (search.getMin()) || null != (search.getMax())) {

				if (select.endsWith(")"))
					select += " and";
				select += " ( p.book = b.id )";

				if (null != (search.getMin())) {
					if (select.endsWith(")"))
						select += " and";
					select += " ( p.value >= :min )";
				}

				if (null != (search.getMax())) {
					if (select.endsWith(")"))
						select += " and";
					select += " ( p.value <= :max )";
				}
			}

			if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
				if (select.endsWith(")"))
					select += " and";
				select += " ( t.book = b.id and t.tag in :tags )";
			}

			if (StringUtils.isNoneEmpty(search.getSerie())) {
				if (select.endsWith(")"))
					select += " and";
				select += " (b.id = sl.book and sl.series = s.id and s.name like :serie )";
			}

		}

		select += " order by b." + sort + " " + order;

		Query query = entityManager.createQuery(select);
		if (search.getIni() != null) {
			query.setParameter("ini", sdf.format(search.getIni()));
		}
		if (search.getEnd() != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(search.getEnd());
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			query.setParameter("end", sdf.format(c.getTime()));
		}
		if (search.getMin() != null) {
			query.setParameter("min", search.getMin().intValue());
		}
		if (search.getMax() != null) {
			query.setParameter("max", search.getMax().intValue());
		}
		if (!CollectionUtils.isEmpty(search.getSelectedTags())) {
			List<Integer> tags = new ArrayList<>(search.getSelectedTags().size());
			for (Tag tag : search.getSelectedTags()) {
				tags.add(tag.getId());
			}
			query.setParameter("tags", tags);
		}
		if (StringUtils.isNoneEmpty(search.getSerie())) {
			query.setParameter("serie", "%" + search.getSerie() + "%");
		}

		return query.setFirstResult(page * size).setMaxResults(size).getResultList();
	}

}
