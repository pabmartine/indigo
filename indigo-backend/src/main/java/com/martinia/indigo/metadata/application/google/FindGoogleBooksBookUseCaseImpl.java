package com.martinia.indigo.metadata.application.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.application.BookMetadataTitles;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "flags.google", havingValue = "true")
public class FindGoogleBooksBookUseCaseImpl implements FindGoogleBooksBookUseCase {
    @Value("${metadata.google.url}")
    private String endpoint;
    @Value("${metadata.google.api-key:}")
    private String apiKey;
    @Resource
    private DataUtils dataUtils;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public BookMetadataResult findBook(String title, List<String> authors) {
        if (StringUtils.isBlank(title)) return null;
        List<String> safeAuthors = authors == null ? List.of() : authors.stream()
                .filter(StringUtils::isNotBlank).toList();
        try {
            String template = safeAuthors.isEmpty() ? endpoint.replace("+inauthor:$author", "") : endpoint;
            String requestUrl = template.replace("$title", encode(title))
                    .replace("$author", encode(String.join(" ", safeAuthors)));
            if (StringUtils.isNotBlank(apiKey)) {
                requestUrl += (requestUrl.contains("?") ? "&" : "?") + "key=" + encode(apiKey.trim());
            }
            String json = dataUtils.getData(requestUrl);
            if (StringUtils.isBlank(json)) return null;
            JsonNode response = mapper.readTree(json);
            if (!response.isObject() || response.has("error")
                    || (response.has("items") && !response.get("items").isArray())) {
                throw new IllegalStateException("Invalid Google Books response");
            }
            // An incomplete or malformed candidate must not hide a later valid result.
            for (JsonNode item : response.path("items")) {
                JsonNode volume = item.path("volumeInfo");
                if (!BookMetadataTitles.matches(title, volume.path("title").asText(null),
                        volume.path("subtitle").asText(null)) || !matchesAuthors(volume, safeAuthors)) continue;
                Float average = rating(volume.path("averageRating"));
                if (average == null) continue;
                Long count = count(volume.path("ratingsCount"));
                return BookMetadataResult.builder().ratingAverage(average).ratingsCount(count)
                        .provider(ProviderEnum.GOOGLE.name()).matchConfidence(1D).build();
            }
            return null;
        } catch (Exception exception) {
            com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
            throw new IllegalStateException("Could not obtain Google Books metadata for " + title, exception);
        }
    }

    private boolean matchesAuthors(JsonNode volume, List<String> authors) {
        if (authors.isEmpty()) return true;
        for (JsonNode candidate : volume.path("authors")) {
            String actual = normalize(candidate.asText());
            if (!actual.isBlank() && authors.stream().map(this::normalize).anyMatch(actual::equals)) return true;
        }
        return false;
    }

    private Float rating(JsonNode value) {
        try {
            float number = Float.parseFloat(value.asText());
            return Float.isFinite(number) && number >= 0 && number <= 5 ? number : null;
        } catch (NumberFormatException exception) { return null; }
    }

    private Long count(JsonNode value) {
        try {
            long number = Long.parseLong(value.asText());
            return number >= 0 ? number : null;
        } catch (NumberFormatException exception) { return null; }
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private String normalize(String value) {
        return StringUtils.stripAccents(StringUtils.defaultString(value)).replaceAll("[^a-zA-Z0-9]", " ")
                .replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT).trim();
    }
}
