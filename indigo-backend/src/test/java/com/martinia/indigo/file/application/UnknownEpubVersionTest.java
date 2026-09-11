package com.martinia.indigo.file.application;

import com.martinia.indigo.common.util.XmlUtils;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class UnknownEpubVersionTest {
    @ParameterizedTest @ValueSource(strings = {"unknown", "null", "NaN", "Infinity", "-1"})
    void unknownVersionDoesNotInvalidateTheBook(String version) {
        String xml = """
                <package xmlns:dc="http://purl.org/dc/elements/1.1/"><metadata>
                <dc:title>Book</dc:title><dc:creator>Author</dc:creator><dc:language>es</dc:language>
                <meta name="calibre:user_metadata:#version" content="{&quot;#value#&quot;: %s, &quot;other&quot;: 0}"/>
                </metadata></package>
                """.formatted(version);
        var book = XmlUtils.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(book);
        assertEquals(0F, book.getVersion());
    }
}
