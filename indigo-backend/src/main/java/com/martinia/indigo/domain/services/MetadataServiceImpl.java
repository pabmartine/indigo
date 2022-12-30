package com.martinia.indigo.domain.services;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

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

    private void fillAuthors(String id, Book book) {

        boolean updateBook = false;

        // Authors
        if (!CollectionUtils.isEmpty(book.getAuthors())) {

            List<Author> authors = calibreRepository.findAuthorsByBook(id);

            if (!CollectionUtils.isEmpty(authors)) {

                for (Author author : authors) {

                    if (author.getName().equals("VV., AA.") || author.getSort().equals("VV., AA.")) {
                        author.setName("AA. VV.");
                        author.setSort("AA. VV.");
                    }

                    if (!book.getAuthors().contains(author.getSort())) {

                        String[] tokens = author.getSort().replace(",", "").split(" ");
                        boolean _contains = false;
                        for (String a : book.getAuthors()) {
                            boolean contains = true;
                            for (String t : tokens) {
                                if (!a.contains(t)) {
                                    contains = false;
                                    break;
                                }
                            }
                            if (contains) {
                                _contains = true;

                                if (!a.equals(author.getSort())) {
                                    author.setSort(a);
                                    updateBook = true;
                                }
                            }
                        }

                        if (!_contains) {
                            book.getAuthors().add(author.getSort());
                            updateBook = true;
                        }

                    }

                    com.martinia.indigo.domain.model.Author domainAuthor = new com.martinia.indigo.domain.model.Author();
                    domainAuthor.setName(author.getName());
                    domainAuthor.setSort(author.getSort());
                    domainAuthor.setNumBooks(new NumBooks());
                    for (String lang : book.getLanguages()) {
                        domainAuthor.getNumBooks().getLanguages().put(lang, 1);
                    }

                    authorRepository.save(domainAuthor);
                }

                if (updateBook == true) {
                    String bookId = bookRepository.findByPath(book.getPath()).get().getId();
                    book.setId(bookId);
                    bookRepository.save(book);
                }
            }

        }


    }


    private void fillMetadataAuthors(String lang, boolean override) {

        metadataSingleton.setMessage("obtaining_metadata_authors");

        List<String> languages = bookRepository.getBookLanguages();
        Long numAuthors = authorRepository.count(languages);

        metadataSingleton.setTotal(metadataSingleton.getTotal() + numAuthors);

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

                    metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

                    author = findAuthorMetadata(lang, override, author);

                    authorRepository.update(author);

                    log.debug("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);

                }
            }

            log.info("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);
            page++;

        }

        log.info("Obtained {}/{} authors metadata", metadataSingleton.getCurrent(), numAuthors);


    }


    private void fillMetadataBooks(boolean override) {

        metadataSingleton.setMessage("obtaining_metadata_books");

        Long numBooks = bookRepository.count(null);

        metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

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

                    metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

                    book = findBookRecommendations(book);
                    book = findBookMetadata(override, book);

                    bookRepository.save(book);

                    log.debug("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
                }
            }

            log.info("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);
            page++;

        }

        log.info("Obtained {}/{} books metadata", metadataSingleton.getCurrent(), numBooks);

    }

    private Author findAuthorMetadata(String lang, boolean override, Author author) {
        if (override || author == null || StringUtils.isEmpty(author.getDescription())
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
                    String[] goodReads = goodReadsComponent.findAuthor(goodreads, author.getName());
                    if (goodReads != null) {
                        author.setDescription(goodReads[0]);
                        author.setImage(goodReads[1]);
                        author.setProvider(goodReads[2]);

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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return author;
    }

    @Override
    public Optional<Author> findAuthorMetadata(String sort, String lang) {

        return authorRepository.findBySort(sort).map(author -> {
            Author _author = findAuthorMetadata(lang, true, author);
            authorRepository.update(_author);
            return Optional.of(_author);
        }).orElse(Optional.empty());

    }

    private Book findBookMetadata(boolean override, Book book) {
        if (override || book.getRating() == 0 || book.getProvider() == null
                || CollectionUtils.isEmpty(book.getSimilar())) {

            try {

                long seconds = ((System.currentTimeMillis() - lastExecution) / 1000);

                if (seconds < 1) {
                    Thread.sleep(pullTime);
                }

                String[] bookData = goodReadsComponent.findBook(goodreads, book.getTitle(), book.getAuthors(), false);

                lastExecution = System.currentTimeMillis();

                // Ratings && similar books
                if (bookData != null) {
                    book.setRating(Float.valueOf(bookData[0]));
                    book.setProvider(bookData[2]);

                    if (StringUtils.isNotEmpty(bookData[1])) {
                        List<String> similars = findSimilarBooks(bookData[1]);
                        if (!CollectionUtils.isEmpty(similars))
                            book.setSimilar(similars);
                    }

                } else {
                    bookData = googleBooksComponent.findBook(book.getTitle(), book.getAuthors());

                    if (bookData != null) {
                        book.setRating(Float.valueOf(bookData[0]));
                        book.setProvider(bookData[1]);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return book;
    }

    @Override
    public Optional<Book> findBookMetadata(String path) {

        if (goodreads == null) {
            goodreads = configurationRepository.findByKey("goodreads.key").get().getValue();
        }

        return bookRepository.findByPath(path).map(book -> {
            Book _book = findBookMetadata(true, book);
            bookRepository.save(_book);
            return Optional.of(_book);
        }).orElse(Optional.empty());

    }

    private Book findBookRecommendations(Book book) {

        List<Book> recommendations = bookRepository.getRecommendationsByBook(book);
        if (!CollectionUtils.isEmpty(recommendations)) {
            book.setRecommendations(new ArrayList<>());
            recommendations.forEach(b -> book.getRecommendations().add(b.getId()));
        }

        return book;
    }

    private List<String> findSimilarBooks(String similar) {

        List<String> ret = new ArrayList<>();

        String[] similars = similar.split("#;#");
        for (String s : similars) {
            String[] data = s.split("@;@");
            String title = data[0];
            String author = data[1];

            Search search = new Search();
            while (title.contains("(") && title.contains(")")) {
                String del = title.substring(title.indexOf("("), title.indexOf(")") + 1);
                title = title.replace(del, "");
            }
            while (title.contains("[") && title.contains("]")) {
                String del = title.substring(title.indexOf("["), title.indexOf("]") + 1);
                title = title.replace(del, "");
            }

            search.setTitle(title.replace("+", "").replace("¿", "").replace("?", "").replace("*", "").replace("(", "")
                    .replace(")", "").replace("[", "").replace("]", ""));

            List<Book> books = bookRepository.findAll(search, 0, Integer.MAX_VALUE, "_id", "asc");
            if (!CollectionUtils.isEmpty(books))
                for (Book book : books) {
                    String _authors = String.join(" ", book.getAuthors());

                    String filterSimilarAuthor = StringUtils.stripAccents(author).replaceAll("[^a-zA-Z0-9]", " ")
                            .replaceAll("\\s+", " ").toLowerCase().trim();

                    String[] similarTerms = StringUtils.stripAccents(_authors).replaceAll("[^a-zA-Z0-9]", " ")
                            .replaceAll("\\s+", " ").split(" ");

                    boolean similarContains = true;
                    for (String similarTerm : similarTerms) {
                        similarTerm = StringUtils.stripAccents(similarTerm).toLowerCase().trim();
                        if (!filterSimilarAuthor.contains(similarTerm)) {
                            similarContains = false;
                        }
                    }

                    if (similarContains) {
                        ret.add(book.getId());
                    }
                }
        }

        return ret;
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

    private void initialLoad(String lang) {

        metadataSingleton.setMessage("indexing_books");

        tagRepository.dropCollection();
        authorRepository.dropCollection();
        bookRepository.dropCollection();

        Long numBooks = calibreRepository.count(null);
        metadataSingleton.setTotal(metadataSingleton.getTotal() + numBooks);

        int page = 0;
        int size = BATCH_SIZE;

        while (page * size < numBooks) {

            if (!metadataSingleton.isRunning())
                break;

            List<Book> books = calibreRepository.findAll(null, page, size, "id", "asc");

            for (Book book : books) {
                if (!metadataSingleton.isRunning())
                    break;

                metadataSingleton.setCurrent(metadataSingleton.getCurrent() + 1);

                try {
                    String id = book.getId();
                    String image = utilComponent.getBase64Cover(book.getPath());
                    book.setImage(image);
                    book.setId(null);
                    tagRepository.save(book.getTags(), book.getLanguages());
                    bookRepository.save(book);

                    fillAuthors(id, book);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                log.debug("Indexed {}/{} books", metadataSingleton.getCurrent(), numBooks);

            }

            log.info("Indexed {}/{} books", metadataSingleton.getCurrent(), numBooks);

            page++;

        }

        fillMetadataBooks(true);
        fillMetadataAuthors(lang, true);

        stop();
    }

    private void noFilledMetadata(String lang) {

        metadataSingleton.setMessage("obtaining_metadata");

        fillMetadataBooks(false);
        fillMetadataAuthors(lang, false);

        stop();
    }

    @Async
    @Override
    public void start(String lang, String type, String entity) {
        log.info("Starting async process");

        goodreads = configurationRepository.findByKey("goodreads.key").get().getValue();

        pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull").get().getValue());

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

    public void stop() {
        log.info("Stopping async process");
        metadataSingleton.stop();
    }


}
