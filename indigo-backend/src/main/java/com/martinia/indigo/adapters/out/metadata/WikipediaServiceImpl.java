package com.martinia.indigo.adapters.out.metadata;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.domain.util.DataUtils;
import com.martinia.indigo.ports.out.metadata.WikipediaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WikipediaServiceImpl implements WikipediaService {

  private String PROVIDER = "Wikipedia";

  public static void main(String[] args) {

    // TODO probar con José Luis de Vilallonga
    WikipediaServiceImpl imp = new WikipediaServiceImpl();
    imp.findAuthor("José Luis de Vilallonga", "es", 0);
  }

  @Override
  public String[] findAuthor(String subject, String lang, int cont) {

    String[] ret = null;

    subject = StringUtils.stripAccents(subject)
        .replaceAll("[^a-zA-Z0-9]", " ")
        .replaceAll("\\s+", " ");

    String url = "https://"
        + lang
        + ".wikipedia.org/w/api.php?action=query&format=json&list=search&utf8=1&origin=*&srsearch="
        + subject.replace(" ", "%20");
    try {

      String json = DataUtils.getData(url);

      if (json != null) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode jsonNodeRoot = objectMapper.readTree(json);
        JsonNode query = jsonNodeRoot.get("query");
        JsonNode search = query.get("search");

        String strTitle = null;
        String[] terms = subject.split(" ");
        if (search.isArray() && !search.isEmpty()) {
          for (final JsonNode objNode : search) {
            JsonNode title = objNode.get("title");
            strTitle = title.asText();

            String filterTitle = StringUtils.stripAccents(strTitle)
                .replaceAll("[^a-zA-Z0-9]", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase()
                .trim();

            boolean contains = false;
            int hasTerms = 0;
            for (String term : terms) {
              term = StringUtils.stripAccents(term)
                  .toLowerCase()
                  .trim();
              if (filterTitle.contains(term)) {
                hasTerms++;
              }
            }

            if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
              contains = true;
            }

            if (contains)
              break;
            else
              strTitle = null;

          }
        } else if (query.get("searchinfo") != null
            && query.get("searchinfo")
                .get("suggestion") != null
            && cont < 2) {
          String auth = query.get("searchinfo")
              .get("suggestion")
              .asText();
          if (!StringUtils.stripAccents(auth).equals(StringUtils.stripAccents(subject))) {
            log.warn("	Retry with {}", auth);
            return findAuthor(auth, lang, ++cont);
          }
        }

        if (StringUtils.isNotEmpty(strTitle)) {
          ret = getAuthorInfo(strTitle, lang);
        }
      }
    } catch (Exception e) {
      log.error(url);
      e.printStackTrace();
    }

    return ret;
  }

  @Override
  public String[] getAuthorInfo(String subject, String lang) {

    String[] ret = null;

    subject = StringUtils.stripAccents(subject)
        .replaceAll("[^a-zA-Z0-9]", " ")
        .replaceAll("\\s+", " ");

    String url = "https://"
        + lang
        + ".wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&generator=search&gsrlimit=1&redirects=1&piprop=original&gsrsearch="
        + subject.replace(" ", "%20");
    try {
      String json = DataUtils.getData(url);

      if (json != null) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode jsonNodeRoot = objectMapper.readTree(json);
        JsonNode query = jsonNodeRoot.get("query");
        if (query != null) {

          JsonNode search = query.get("pages");
          JsonNode title = search.findPath("title");
          JsonNode extract = search.findPath("extract");
          JsonNode source = search.findPath("original")
              .get("source");

          String strTitle = title.asText();
          String filterTitle = StringUtils.stripAccents(strTitle)
              .replaceAll("[^a-zA-Z0-9]", " ")
              .replaceAll("\\s+", " ")
              .toLowerCase()
              .trim();

          String[] terms = subject.split(" ");
          boolean contains = false;
          int hasTerms = 0;

          for (String term : terms) {

            term = StringUtils.stripAccents(term)
                .toLowerCase()
                .trim();

            if (filterTitle.contains(term)) {
              hasTerms++;
            }
          }

          if (extract.asText()
              .toLowerCase()
              .startsWith("for the")) {
            log.info(title + " --> " + extract.asText());
          }

          if (terms.length == 1 && hasTerms > 0 || terms.length > 1 && hasTerms > 1) {
            contains = true;
          }

          if (contains) {
            ret = new String[]{extract.asText(), source != null ? source.asText() : null, PROVIDER};
          }
        }
      }
    } catch (Exception e) {
      log.error(url);

      e.printStackTrace();
    }

    return ret;
  }

}
