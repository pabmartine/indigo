package com.martinia.indigo.domain.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.inner.NumBooks;
import com.martinia.indigo.domain.singletons.MetadataSingleton;
import com.martinia.indigo.domain.util.UtilComponent;
import com.martinia.indigo.ports.in.rest.MetadataService;
import com.martinia.indigo.ports.out.calibre.CalibreRepository;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;
import com.martinia.indigo.ports.out.metadata.GoogleBooksService;
import com.martinia.indigo.ports.out.metadata.WikipediaService;
import com.martinia.indigo.ports.out.mongo.AuthorRepository;
import com.martinia.indigo.ports.out.mongo.BookRepository;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import com.martinia.indigo.ports.out.mongo.TagRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private AuthorRepository authorRepository;

  @Autowired
  private ConfigurationRepository configurationRepository;

  @Autowired
  private WikipediaService wikipediaComponent;

  @Autowired
  private GoodReadsService goodReadsComponent;

  @Autowired
  private GoogleBooksService googleBooksComponent;

  @Autowired
  private CalibreRepository calibreRepository;

  @Autowired
  private MetadataSingleton metadataSingleton;

  @Autowired
  private UtilComponent utilComponent;

  private String goodreads;

  private long pullTime;

  private final int BATCH_SIZE = 500;

  @Override
  @Async
  public void initialLoad(String lang) {

    if (metadataSingleton.isRunning()) {
      stop();
    }

    metadataSingleton.start("full");

    metadataSingleton.setMessage("indexing_books");

    goodreads = configurationRepository.findByKey("goodreads.key")
        .getValue();
    pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull")
        .getValue());

    tagRepository.dropCollection();
    authorRepository.dropCollection();
    bookRepository.dropCollection();

    Long numBooks = calibreRepository.count(null);
    metadataSingleton.setTotal(numBooks);

    int cont = 0;
    int page = 0;
    int size = BATCH_SIZE;

    while (page * size < numBooks) {

      if (!metadataSingleton.isRunning())
        break;

      List<Book> books = calibreRepository.findAll(null, page, size, "id", "asc");

      for (Book book : books) {

        book.setId(null);

        if (!metadataSingleton.isRunning())
          break;

        metadataSingleton.setCurrent(cont++);

        try {
          String image = utilComponent.getBase64Cover(book.getPath());
          book.setImage(image);

          tagRepository.save(book.getTags(), book.getLanguages());
          bookRepository.save(book);
        } catch (Exception e) {
          e.printStackTrace();
        }

        log.debug("Indexed {}/{} books", cont, numBooks);

      }

      log.info("Indexed {}/{} books", cont, numBooks);

      page++;

    }

    fillAuthors();
    fillRecommendations();
    fillMetadataBooks(lang, true);
    fillMetadataAuthors(lang, true);
    stop();
  }

  private void fillAuthors() {

    metadataSingleton.setMessage("filling_authors");

    Long numBooks = bookRepository.count(null);

    metadataSingleton.setTotal(numBooks);

    int cont = 0;
    int page = 0;
    int size = BATCH_SIZE;
    while (page * size < numBooks) {

      if (!metadataSingleton.isRunning())
        break;

      List<Book> books = calibreRepository.findAll(null, page, size, "id", "asc");

      for (Book book : books) {

        if (!metadataSingleton.isRunning())
          break;

        metadataSingleton.setCurrent(cont++);

        // Authors
        if (!CollectionUtils.isEmpty(book.getAuthors())) {

          List<Author> authors = calibreRepository.findAuthorsByBook(book.getId());

          if (!CollectionUtils.isEmpty(authors)) {

            for (Author author : authors) {

              com.martinia.indigo.domain.model.Author domainAuthor = new com.martinia.indigo.domain.model.Author();
              domainAuthor.setName(author.getName());
              domainAuthor.setSort(author.getSort());
              domainAuthor.setNumBooks(new NumBooks());
              for (String lang : book.getLanguages()) {
                domainAuthor.getNumBooks()
                    .getLanguages()
                    .put(lang, 1);
              }

              authorRepository.save(domainAuthor);

            }

          }

        }

        log.debug("Filling {}/{} authors", cont, numBooks);

      }

      log.info("Filled {}/{} authors", cont, numBooks);
      page++;
    }

  }

  private void fillRecommendations() {

    metadataSingleton.setMessage("filling_recommendations");

    Long numBooks = bookRepository.count(null);

    metadataSingleton.setTotal(numBooks);

    int cont = 0;
    int page = 0;
    int size = BATCH_SIZE;
    while (page * size < numBooks) {

      if (!metadataSingleton.isRunning())
        break;

      List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

      for (Book book : books) {

        if (!metadataSingleton.isRunning())
          break;

        metadataSingleton.setCurrent(cont++);

        List<Book> recommendations = bookRepository.getRecommendationsByBook(book);
        if (!CollectionUtils.isEmpty(recommendations)) {
          book.setRecommendations(new ArrayList<>());
          recommendations.forEach(b -> book.getRecommendations()
              .add(b.getId()));
          bookRepository.save(book);
        }

        log.debug("Generated {}/{} recommendations", cont, numBooks);

      }

      log.info("Generated {}/{} recommendations", cont, numBooks);
      page++;
    }

  }

  private void fillMetadataBooks(String lang, boolean override) {

    metadataSingleton.setMessage("obtaining_metadata_books");

    Long numBooks = bookRepository.count(null);

    metadataSingleton.setTotal(numBooks);

    int cont = 0;
    int page = 0;
    int size = BATCH_SIZE;
    while (page * size < numBooks) {

      if (!metadataSingleton.isRunning())
        break;

      List<Book> books = bookRepository.findAll(null, page, size, "id", "asc");

      if (!CollectionUtils.isEmpty(books)) {
        for (Book book : books) {

          if (!metadataSingleton.isRunning())
            break;

          metadataSingleton.setCurrent(cont++);

          if (override
              || book.getRating() == 0 || book.getProvider() == null
              || CollectionUtils.isEmpty(book.getSimilar())) {

            try {

              String[] goodReads = goodReadsComponent.findBook(goodreads, books, book.getTitle(),
                  book.getAuthors(), false);

              boolean updateBook = false;
              // Ratings && similar books
              if (goodReads != null) {
                book.setRating(Float.valueOf(goodReads[0]));
                book.setProvider(goodReads[2]);

                if (StringUtils.isNotEmpty(goodReads[1]))
                  book.setSimilar(Arrays.asList(goodReads[1].split("\\\\s*;\\\\s*")));

                updateBook = true;

                Thread.sleep(pullTime);

              } else {
                String[] googleBooks = googleBooksComponent.findBook(book.getTitle(),
                    book.getAuthors());

                if (googleBooks != null) {
                  book.setRating(Float.valueOf(googleBooks[0]));
                  book.setProvider(googleBooks[1]);

                  updateBook = true;
                }
              }

              if (updateBook) {
                bookRepository.save(book);
              }

              log.debug("Obtained {}/{} books metadata", cont, numBooks);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
      page++;

    }

    log.info("Obtained {}/{} books metadata", cont, numBooks);

  }

  private void fillMetadataAuthors(String lang, boolean override) {

    metadataSingleton.setMessage("obtaining_metadata_authors");

    List<String> languages = bookRepository.getBookLanguages();
    Long numAuthors = authorRepository.count(languages);

    metadataSingleton.setTotal(numAuthors);

    int cont = 0;
    int page = 0;
    int size = BATCH_SIZE;
    while (page * size < numAuthors) {

      if (!metadataSingleton.isRunning())
        break;

      List<Author> authors = authorRepository.findAll(languages, page, size, "id", "asc");

      if (!CollectionUtils.isEmpty(authors)) {
        for (Author author : authors) {

          if (!metadataSingleton.isRunning())
            break;

          metadataSingleton.setCurrent(cont++);

          if (override
              || author == null || StringUtils.isEmpty(author.getDescription())
              || StringUtils.isEmpty(author.getImage()) || StringUtils.isEmpty(author.getProvider())) {

            try {

              String[] wikipedia = wikipediaComponent.findAuthor(author.getName(), lang, 0);

              if (wikipedia == null && !lang.equals("en")) {
                wikipedia = wikipediaComponent.findAuthor(author.getName(), "en", 0);
              }

              if (wikipedia == null || wikipedia[1] == null) {
                String[] _goodReads = goodReadsComponent.findAuthor(goodreads, author.getName());
                if (_goodReads != null) {
                  author.setDescription(_goodReads[0]);
                  author.setImage(_goodReads[1]);
                  author.setProvider(_goodReads[2]);

                  Thread.sleep(pullTime);
                }

              }

              if (wikipedia != null) {
                if (StringUtils.isEmpty(author.getDescription()) && !StringUtils.isEmpty(wikipedia[0]))
                  author.setDescription(wikipedia[0]);
                if (StringUtils.isEmpty(author.getImage()) && !StringUtils.isEmpty(wikipedia[1]))
                  author.setImage(wikipedia[1]);
                if (StringUtils.isEmpty(author.getProvider()) && !StringUtils.isEmpty(wikipedia[2]))
                  author.setProvider(wikipedia[2]);
              }

              if (!StringUtils.isEmpty(author.getDescription())
                  || !StringUtils.isEmpty(author.getImage())) {
                author.setImage(utilComponent.getBase64Url(author.getImage()));
                authorRepository.update(author);
              } else {
                log.warn("Not found data for author {}", author.getName());
              }

              log.debug("Obtained {}/{} authors metadata", cont - numAuthors, numAuthors);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }

      page++;

    }

    log.info("Obtained {}/{} authors metadata", cont - numAuthors, numAuthors);

  }

  @Override
  @Async
  public void noFilledMetadata(String lang) {

    if (metadataSingleton.isRunning()) {
      stop();
    }

    metadataSingleton.start("partial");
    metadataSingleton.setMessage("obtaining_metadata");

    goodreads = configurationRepository.findByKey("goodreads.key")
        .getValue();
    pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull")
        .getValue());

    Long numBooks = bookRepository.count(null);
    metadataSingleton.setTotal(numBooks);

    fillMetadataBooks(lang, false);
    fillMetadataAuthors(lang, false);

    stop();
  }

  public Map<String, Object> getStatus() {
    Map<String, Object> data = new HashMap<>();
    data.put("type", metadataSingleton.getType());
    data.put("status", metadataSingleton.isRunning());
    data.put("current", metadataSingleton.getCurrent());
    data.put("total", metadataSingleton.getTotal());
    data.put("message", metadataSingleton.getMessage());
    return data;
  }

  public void stop() {
    log.info("Stopping async authors process");
    metadataSingleton.stop();
  }

}
