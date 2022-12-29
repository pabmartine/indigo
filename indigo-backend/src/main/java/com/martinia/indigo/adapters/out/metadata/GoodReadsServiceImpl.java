package com.martinia.indigo.adapters.out.metadata;

import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.ports.out.metadata.GoodReadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodReadsServiceImpl implements GoodReadsService {

    private String endpoint = "https://www.goodreads.com/";

    private String PROVIDER = "Goodreads";

    @Override
    public String[] findBook(String key, String title, List<String> authors, boolean withAuthor) {

        String[] ret = null;

        try {

            String author = String.join(" ", authors);

            author = StringUtils.stripAccents(author)
                    .replaceAll("[^a-zA-Z0-9]", " ")
                    .replaceAll("\\s+", " ");
            title = StringUtils.stripAccents(title.replaceAll("ñ", "-"))
                    .replaceAll("[^a-zA-Z0-9]", " ")
                    .replaceAll("\\s+", " ");

            String url = endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key;
            if (withAuthor) {
                url += "&authors=" + author.replace(" ", "%20");
            }

            String xml = DataUtils.getData(url);

            if (StringUtils.isNoneEmpty(xml)) {
                Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

                if (doc.select("book")
                        .first() != null) {

                    String finalTitle = title;
                    String finalAuthor = author;
                    ret = doc.select("book").stream().map(book -> {

                        String name = book
                                .select("title")
                                .text();
                        String filterName = StringUtils.stripAccents(name)
                                .replaceAll("[^a-zA-Z0-9]", " ")
                                .replaceAll("\\s+", " ")
                                .toLowerCase()
                                .trim();

                        String[] terms = finalTitle.split(" ");

                        long hasTerms = Arrays.stream(terms).filter(term -> {
                            return (filterName.contains(StringUtils.stripAccents(term)
                                    .toLowerCase()
                                    .trim()));
                        }).count();


                        if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

                            String _author = book
                                    .select("authors")
                                    .select("author")
                                    .select("name")
                                    .text();

                            String filterAuthor = StringUtils.stripAccents(_author)
                                    .replaceAll("[^a-zA-Z0-9]", " ")
                                    .replaceAll("\\s+", " ")
                                    .toLowerCase()
                                    .trim();

                            terms = finalAuthor.split(" ");

                            hasTerms = Arrays.stream(terms).filter(term -> {
                                return (filterAuthor.contains(StringUtils.stripAccents(term)
                                        .toLowerCase()
                                        .trim()));
                            }).count();


                            if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {

                                float rating = Float.parseFloat(book
                                        .select("average_rating")
                                        .get(0)
                                        .text());
                                String similar = book
                                        .select("similar_books")
                                        .select("book").stream().map(similarBook -> {
                                            String similar_title = similarBook
                                                    .select("title")
                                                    .text();
                                            String similar_author = similarBook
                                                    .select("authors")
                                                    .select("author")
                                                    .select("name")
                                                    .text();

                                            return similar_title + "@;@" + similar_author;
                                        }).collect(Collectors.joining("#;#"));


                                if (StringUtils.isNoneEmpty(similar))
                                    return new String[]{String.valueOf(rating),
                                            similar,
                                            PROVIDER};

                            }

                        }
                        return null;
                    }).filter(Objects::nonNull).findFirst().orElse(new String[0]);

                }
            }
        } catch (Exception e) {
            log.error(endpoint + "book/title.xml?title=" + title.replace(" ", "-") + "&key=" + key);
        }

        if (ret.length == 0 && !withAuthor) {
            ret = findBook(key, title, authors, true);
        }

        return ret;

    }

    @Override
    public String[] findAuthor(String key, String subject) {

        String[] ret = null;

        try {

            subject = StringUtils.stripAccents(subject)
                    .replaceAll("[^a-zA-Z0-9]", " ")
                    .replaceAll("\\s+", " ");

            String url = endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key;
            String xml = DataUtils.getData(url);

            if (StringUtils.isNoneEmpty(xml)) {
                Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
                if (doc.select("author")
                        .first() != null) {
                    String name = doc.select("author")
                            .select("name")
                            .get(0)
                            .text();
                    String id = doc.select("author")
                            .select("id")
                            .get(0)
                            .text();

                    if (name != null && id != null) {

                        String filterName = StringUtils.stripAccents(name)
                                .replaceAll("[^a-zA-Z0-9]", " ")
                                .replaceAll("\\s+", " ")
                                .toLowerCase()
                                .trim();

                        String[] terms = subject.split(" ");

                        long hasTerms = Arrays.stream(terms).filter(term -> {
                            return (filterName.contains(StringUtils.stripAccents(term)
                                    .toLowerCase()
                                    .trim()));
                        }).count();

                        if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1)
                            ret = getAuthorInfo(key, id);
                        else
                            log.debug("************************** " + subject + " is not contained in " + name);

                    }
                }
            }
        } catch (Exception e) {
            log.error(endpoint + "search.xml?q=" + subject.replace(" ", "+") + "&key=" + key);
        }

        return ret;

    }

    private String[] getAuthorInfo(String key, String id) {

        String[] ret = null;

        try {
            String url = endpoint + "author/show/" + id + "?format=xml&key=" + key;
            String xml = DataUtils.getData(url);

            if (xml != null) {
                Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
                if (doc.select("author")
                        .first() != null) {
                    String name = doc.select("author")
                            .select("name")
                            .get(0)
                            .text();
                    String description = doc.select("author")
                            .select("about")
                            .text();
                    String image = doc.select("author")
                            .select("image_url")
                            .get(0)
                            .text();

                    if (StringUtils.isNotEmpty(name)) {
                        ret = new String[]{description, image, PROVIDER};
                    }
                }
            }

        } catch (Exception e) {
            log.error(endpoint + "author/show/" + id + "?format=xml&key=" + key);
        }

        return ret;

    }

}
