package com.martinia.indigo.adapters.out.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.ports.out.metadata.GoogleBooksService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleBooksServiceImpl implements GoogleBooksService {

  private String endpoint = "https://www.googleapis.com/books/v1/volumes?q=";

  private String PROVIDER = "Google Books";

  @Override
  public String[] findBook(String title, List<String> authors) {

    String[] ret = null;

    try {

      String author = String.join(" ", authors);

      author = StringUtils.stripAccents(author)
          .replaceAll("[^a-zA-Z0-9]", " ")
          .replaceAll("\\s+", " ");
      title = StringUtils.stripAccents(title.replaceAll("ñ", "-"))
          .replaceAll("[^a-zA-Z0-9]", " ")
          .replaceAll("\\s+", " ")
          .replaceAll(" ", "%20");

      String url = endpoint + "intitle:" + title;
      String json = DataUtils.getData(url);

      if (json != null) {
        JsonParser springParser = JsonParserFactory.getJsonParser();
        Map<String, Object> map = springParser.parseMap(json);

        if (map.containsKey("items")) {
          ArrayList<LinkedHashMap<String, Object>> items = (ArrayList<LinkedHashMap<String, Object>>) map
              .get("items");
          for (LinkedHashMap<String, Object> item : items) {
            LinkedHashMap<String, Object> volumeInfo = (LinkedHashMap<String, Object>) item
                .get("volumeInfo");

            String name;
            try {
              name = volumeInfo.get("title")
                  .toString();
            } catch (Exception e) {
              throw e;
            }
            String filterName = StringUtils.stripAccents(name)
                .replaceAll("[^a-zA-Z0-9]", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase()
                .trim();

            String[] terms = title.split("%20");
            boolean contains = true;
            int hasTerms = 0;
            for (String term : terms) {

              term = StringUtils.stripAccents(term)
                  .toLowerCase()
                  .trim();

              if (!filterName.contains(term)) {
                hasTerms++;
              }
            }

            if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
              contains = true;
            }

            if (contains) {
              ArrayList<String> _authors = (ArrayList<String>) volumeInfo.get("authors");
              if (_authors != null)
                for (String _author : _authors) {

                  String filterAuthor = StringUtils.stripAccents(_author)
                      .replaceAll("[^a-zA-Z0-9]", " ")
                      .replaceAll("\\s+", " ")
                      .toLowerCase()
                      .trim();

                  terms = author.split(" ");

                  contains = true;
                  hasTerms = 0;
                  for (String term : terms) {

                    term = StringUtils.stripAccents(term)
                        .toLowerCase()
                        .trim();

                    if (!filterAuthor.contains(term)) {
                      hasTerms++;
                    }
                  }

                  if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
                    contains = true;
                  }

                  if (contains) {
                    if (volumeInfo.get("averageRating") != null) {
                      String rating = volumeInfo.get("averageRating")
                          .toString();
                      ret = new String[]{rating, PROVIDER};
                      break;
                    }
                  }
                }

              if (ret != null) {
                break;
              }
            }
          }
        }

      }

    } catch (

    Exception e) {
      log.error(endpoint + "intitle:" + title);
      e.printStackTrace();
    }

    return ret;
  }
}
