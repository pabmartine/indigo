package com.martinia.indigo.metadata.application.libretranslate;

import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort;
import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class CachedSpanishTranslation {
    @org.springframework.beans.factory.annotation.Autowired private MongoTemplate mongo;
    @Resource private Optional<TranslateLibreTranslatePort> translator;
    @Resource private Optional<DetectLibreTranslatePort> detector = Optional.empty();

    public record Result(String text, String originalLanguage, String language) {}

    public static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return null;
        String normalized = language.trim().toLowerCase(java.util.Locale.ROOT).split("[-_]", 2)[0];
        return "und".equals(normalized) || "auto".equals(normalized) ? null : normalized;
    }

    public synchronized String translate(String text) {
        Result result = translate(text, null);
        return "es".equals(result.language()) ? result.text() : null;
    }

    public synchronized Result translate(String text, String knownLanguage) {
        String source = normalizeLanguage(knownLanguage);
        if (text == null || text.isBlank() || "es".equals(source)) return new Result(text, source, source);
        try {
            String key = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(("detected-v2\nes\n" + source + "\n" + text).getBytes(StandardCharsets.UTF_8)));
            Document cached = mongo.findById(key, Document.class, "metadataTranslations");
            if (cached != null) return new Result(cached.getString("translation"), cached.getString("source"), "es");
            if (source == null) source = normalizeLanguage(detector.map(port -> port.detect(text)).orElse(null));
            if (source == null) return new Result(text, null, null);
            String translated = "es".equals(source) ? text : translator.map(port -> port.translate(text, "es")).orElse(null);
            if (translated == null || translated.isBlank()) return new Result(text, source, source);
            mongo.save(new Document("_id", key).append("target", "es").append("translation", translated)
                    .append("source", source)
                    .append("createdAt", new java.util.Date()), "metadataTranslations");
            return new Result(translated, source, "es");
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
