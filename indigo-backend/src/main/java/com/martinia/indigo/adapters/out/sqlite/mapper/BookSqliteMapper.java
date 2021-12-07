package com.martinia.indigo.adapters.out.sqlite.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.sqlite.entities.BookSqliteEntity;
import com.martinia.indigo.adapters.out.sqlite.repository.CommentSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.repository.LanguageSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.repository.PageSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.repository.SerieSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.repository.TagSqliteRepository;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.inner.Serie;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BookSqliteMapper {

	@Autowired
	private PageSqliteRepository pageSqliteRepository;

	@Autowired
	private SerieSqliteRepository serieSqliteRepository;

	@Autowired
	private TagSqliteRepository tagSqliteRepository;

	@Autowired
	private CommentSqliteRepository commentSqliteRepository;

	@Autowired
	private LanguageSqliteRepository languageSqliteRepository;

	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
	private static SimpleDateFormat SDF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");

	private Date parseDate(String strDate) {
		Date date = null;
		try {
			if (strDate.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\+\\d{2}:\\d{2}$"))
				date = SDF.parse(strDate);
			else if (strDate.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}\\+\\d{2}:\\d{2}$"))
				date = SDF2.parse(strDate);
			else {
				log.error("Unknown date format {}", strDate);
			}
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return date;
	}

	public List<Book> entities2Domains(List<BookSqliteEntity> entities) {
		List<Book> domains = new ArrayList<>(entities.size());
		for (BookSqliteEntity entity : entities) {
			Book domain = new Book();
			domain.setId(String.valueOf(entity.getId()));
			domain.setTitle(entity.getTitle());
			domain.setPubDate(parseDate(entity.getPubDate()));
			domain.setLastModified(parseDate(entity.getLastModified()));
			domain.setPath(entity.getPath());
			domain.setSerie(new Serie(entity.getSeriesIndex()
					.intValue(), serieSqliteRepository.getSerieByBook(entity.getId())));
			domain.setAuthors(new ArrayList<>());
			String[] authors = entity.getAuthorSort()
					.split("&");
			for (String author : authors) {
			  if (author.trim().equals("VV., AA.")) {
                author = "AA. VV.";
              }
				domain.getAuthors()
						.add(author.trim());
			}

			domain.setPages(pageSqliteRepository.findPagesByBookId(entity.getId()).orElse(0));

			domain.setTags(tagSqliteRepository.getTagsByBookId(entity.getId()));
			domain.setComment(commentSqliteRepository.findTextByBookId(entity.getId()));
			domain.setLanguages(languageSqliteRepository.getLanguageByBookId(entity.getId()));
			domains.add(domain);
		}
		return domains;

	}

}