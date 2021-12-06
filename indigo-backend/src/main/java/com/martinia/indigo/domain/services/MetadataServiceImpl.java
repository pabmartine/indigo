package com.martinia.indigo.domain.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
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
  
  private long lastExecution; 

  @PostConstruct
  private void init() {
    goodreads = configurationRepository.findByKey("goodreads.key")
        .getValue();
    
    pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull")
        .getValue());
  }

  private void initialLoad(String lang) {

    metadataSingleton.setMessage("indexing_books");

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
    fillMetadataBooks(true);
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

  private void fillMetadataBooks(boolean override) {

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

          findBookMetadata(override, book);
          log.debug("Obtained {}/{} books metadata", cont, numBooks);
        }
      }
      page++;

    }

    log.info("Obtained {}/{} books metadata", cont, numBooks);

  }

  private List<String> findSimilarBooks(String similar) {
    
    log.info("%%%%%%%%%%%%%% init findSimilarBooks");

    List<String> ret = new ArrayList<>();

    String[] similars = similar.split("#;#");
    for (String s : similars) {
      String[] data = s.split("@;@");
      String title = data[0];
      String author = data[1];

      Search search = new Search();
      if (title.contains("(") && title.contains(")")) {
        String del = title.substring(title.indexOf("("), title.indexOf(")") + 1);
        title = title.replace(del, "");
      }
      search.setTitle(title);
      List<Book> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "_id", "asc");
      if (!CollectionUtils.isEmpty(books))
        for (Book book : books) {
          String _authors = String.join(" ", book.getAuthors());

          String filterSimilarAuthor = StringUtils.stripAccents(author)
              .replaceAll("[^a-zA-Z0-9]", " ")
              .replaceAll("\\s+", " ")
              .toLowerCase()
              .trim();

          String[] similarTerms = StringUtils.stripAccents(_authors)
              .replaceAll("[^a-zA-Z0-9]", " ")
              .replaceAll("\\s+", " ")
              .split(" ");

          boolean similarContains = true;
          for (String similarTerm : similarTerms) {
            similarTerm = StringUtils.stripAccents(similarTerm)
                .toLowerCase()
                .trim();
            if (!filterSimilarAuthor.contains(similarTerm)) {
              similarContains = false;
            }
          }

          if (similarContains) {
            ret.add(book.getId());
          }
        }
    }
    
    log.info("%%%%%%%%%%%%%% end findSimilarBooks");
    
    return ret;
  }

  private Book findBookMetadata(boolean override, Book book) {
    log.info("*****init findBookMetadata");
    if (override
        || book.getRating() == 0 || book.getProvider() == null
        || CollectionUtils.isEmpty(book.getSimilar())) {

      try {
        
        long seconds = ((System.currentTimeMillis()-lastExecution) / 1000);
        log.info("Last execution was " + seconds + " seconds ago");
        
        if (seconds < 1) {
          Thread.sleep(pullTime);
          log.info("Sleep " + pullTime + " second");
        }

        String[] goodReads = goodReadsComponent.findBook(goodreads, book.getTitle(),
            book.getAuthors(), false);
        
        lastExecution = System.currentTimeMillis();

        boolean updateBook = false;
        // Ratings && similar books
        if (goodReads != null) {
          book.setRating(Float.valueOf(goodReads[0]));
          book.setProvider(goodReads[2]);

          if (StringUtils.isNotEmpty(goodReads[1])) {
            List<String> similars = findSimilarBooks(goodReads[1]);
            if (!CollectionUtils.isEmpty(similars))
              book.setSimilar(similars);
          }

          updateBook = true;          

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

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    log.info("*****end findBookMetadata");
    return book;
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

          findAuthorMetadata(lang, override, author);
          log.debug("Obtained {}/{} authors metadata", cont, numAuthors);

        }
      }

      page++;

    }

    log.info("Obtained {}/{} authors metadata", cont, numAuthors);

  }

  private Author findAuthorMetadata(String lang, boolean override, Author author) {
    if (override
        || author == null || StringUtils.isEmpty(author.getDescription())
        || StringUtils.isEmpty(author.getImage()) || StringUtils.isEmpty(author.getProvider())) {

      try {

        author.setDescription(null);
        author.setImage(null);
        author.setProvider(null);

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

        if (!StringUtils.isEmpty(author.getImage()))
          author.setImage(utilComponent.getBase64Url(author.getImage()));

        if (StringUtils.isEmpty(author.getImage())) {
          Search search = new Search();
          search.setAuthor(author.getSort());
          List<Book> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "pubDate", "desc");
          for (Book book : books) {
            String image = utilComponent.getImageFromEpub(book.getPath(), "autor", "author");
            author.setImage(image);
            if (author.getImage() != null)
              break;
          }

        }

        if (override
            || !StringUtils.isEmpty(author.getDescription())
            || !StringUtils.isEmpty(author.getImage())) {
          authorRepository.update(author);
        } else {
          log.warn("Not found data for author {}", author.getName());
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return author;
  }

  private void noFilledMetadata(String lang) {

    metadataSingleton.setMessage("obtaining_metadata");

    Long numBooks = bookRepository.count(null);
    metadataSingleton.setTotal(numBooks);

    fillMetadataBooks(false);
    fillMetadataAuthors(lang, false);

    stop();
  }

  public Map<String, Object> getStatus() {
    Map<String, Object> data = new HashMap<>();
    data.put("type", metadataSingleton.getType());
    data.put("entity", metadataSingleton.getEntity());
    data.put("status", metadataSingleton.isRunning());
    data.put("current", metadataSingleton.getCurrent());
    data.put("total", metadataSingleton.getTotal());
    data.put("message", metadataSingleton.getMessage());
    return data;
  }

  public void stop() {
    log.info("Stopping async process");
    metadataSingleton.stop();
  }

  @Async
  @Override
  public void start(String lang, String type, String entity) {
    log.info("Starting async process");

    if (metadataSingleton.isRunning()) {
      stop();
    }
    metadataSingleton.start(type, entity);

    if (type.equals("full")) {
      if (entity.equals("all")) {
        initialLoad(lang);
      }
      if (entity.equals("authors")) {
        fillMetadataAuthors(lang, true);
      }
      if (entity.equals("books")) {
        fillMetadataBooks(true);
      }
    }

    if (type.equals("partial")) {
      if (entity.equals("all")) {
        noFilledMetadata(lang);
      }
      if (entity.equals("authors")) {
        fillMetadataAuthors(lang, false);
      }
      if (entity.equals("books")) {
        fillMetadataBooks(false);
      }
    }

    if (metadataSingleton.isRunning()) {
      stop();
    }
  }

  @Override
  public Author findAuthorMetadata(String sort, String lang) {

    Author author = authorRepository.findBySort(sort);
    author = findAuthorMetadata(lang, true, author);

    return author;
  }

  @Override
  public Book findBookMetadata(String path) {

    Book book = bookRepository.findByPath(path);
    book = findBookMetadata(true, book);

    return book;
  }

}
