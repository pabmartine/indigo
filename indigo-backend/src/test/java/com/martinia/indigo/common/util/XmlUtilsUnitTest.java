package com.martinia.indigo.common.util;

import com.martinia.indigo.common.domain.model.BookOpf;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

public class XmlUtilsUnitTest {

    @Test
    void parse_WithCompleteOpfXml_ShouldReturnCompleteBookOpf() {
        // Given
        String completeXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata>
                    <dc:title>Test Book Title</dc:title>
                    <dc:creator opf:file-as="Doe, John">John Doe</dc:creator>
                    <dc:description>Test book description</dc:description>
                    <dc:language>en</dc:language>
                    <dc:date opf:event="publication">2023-01-01</dc:date>
                    <dc:subject>Fiction, Adventure</dc:subject>
                    <meta name="calibre:series" content="Test Series"/>
                    <meta name="calibre:series_index" content="2.5"/>
                    <meta name="cover" content="cover.jpg"/>
                    <meta name="autor" content="author.jpg"/>
                </metadata>
            </package>
            """;
        InputStream inputStream = new ByteArrayInputStream(completeXml.getBytes());

        Date mockDate = new Date();
        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
            dateUtilsMock.when(() -> DateUtils.parseDate(anyString())).thenReturn(mockDate);

            // When
            BookOpf result = XmlUtils.parse(inputStream);

            // Then
            assertNotNull(result);
            assertEquals("Test Book Title", result.getTitle());
            assertEquals("Test book description", result.getComment());
            assertNotNull(result.getAuthors());
            assertEquals(1, result.getAuthors().size());
            assertEquals("John Doe", result.getAuthors().get(0));
            assertEquals("Doe, John", result.getAuthorId());
            assertEquals("en", result.getLanguage());
            assertEquals(mockDate, result.getPubDate());
            assertNotNull(result.getTags());
            assertEquals(2, result.getTags().size());
            assertEquals("Fiction", result.getTags().get(0));
            assertEquals("Adventure", result.getTags().get(1));
            assertEquals("Test Series", result.getSeriesName());
            assertEquals(2, result.getSeriesIndex());
            assertEquals("cover.jpg", result.getBookImageName());
            assertEquals("author.jpg", result.getAuthorImageName());
        }
    }

    @Test
    void parse_WithMinimalOpfXml_ShouldReturnBasicBookOpf() {
        // Given
        String minimalXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata>
                    <dc:title>Simple Title</dc:title>
                </metadata>
            </package>
            """;
        InputStream inputStream = new ByteArrayInputStream(minimalXml.getBytes());

        // When
        BookOpf result = XmlUtils.parse(inputStream);

        // Then
        assertNotNull(result);
        assertEquals("Simple Title", result.getTitle());
        assertNull(result.getComment());
        assertTrue(result.getAuthors().isEmpty());
        assertNull(result.getAuthorId());
        assertNull(result.getLanguage());
        assertNull(result.getPubDate());
        assertTrue(result.getTags().isEmpty());
        assertTrue(result.getTranslators().isEmpty());
        assertNull(result.getSeriesName());
        assertEquals(0, result.getSeriesIndex());
        assertEquals(0, result.getPages());
        assertEquals(0F, result.getVersion());
        assertNull(result.getBookImageName());
        assertNull(result.getAuthorImageName());
    }

    @Test
    void parse_WithSeveralSubjectElements_ShouldReturnAllUniqueCategories() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata>
                    <dc:title>Multiple categories</dc:title>
                    <dc:subject>Novela</dc:subject>
                    <dc:subject>Intriga</dc:subject>
                    <dc:subject>novela</dc:subject>
                    <subject>Ciencia ficción; Aventuras</subject>
                </metadata>
            </package>
            """;

        BookOpf result = XmlUtils.parse(new ByteArrayInputStream(xml.getBytes()));

        assertNotNull(result);
        assertEquals(List.of("Novela", "Intriga", "Ciencia ficción", "Aventuras"), result.getTags());
    }

    @Test
    void parse_WithIdentifiers_ShouldNormalizeAndClassifyIsbnValues() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata>
                    <dc:identifier opf:scheme="ISBN">0-306-40615-2</dc:identifier>
                    <dc:identifier>urn:isbn:978-0-306-40615-7</dc:identifier>
                    <dc:identifier>urn:uuid:550e8400-e29b-41d4-a716-446655440000</dc:identifier>
                    <dc:identifier opf:scheme="ISBN">invalid-isbn</dc:identifier>
                </metadata>
            </package>
            """;

        BookOpf result = XmlUtils.parse(new ByteArrayInputStream(xml.getBytes()));

        assertNotNull(result);
        assertEquals(List.of("0306406152"), result.getIsbn10());
        assertEquals(List.of("9780306406157"), result.getIsbn13());
        assertEquals(List.of("550e8400-e29b-41d4-a716-446655440000"), result.getIdentifiers().get("UUID"));
        assertEquals(List.of("invalid-isbn"), result.getIdentifiers().get("ISBN"));
    }

    @Test
    void parse_WithEpub3IdentifierRefinement_ShouldRecognizeIsbn() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata>
                    <dc:identifier id="book-id">9780306406157</dc:identifier>
                    <meta refines="#book-id" property="identifier-type" scheme="onix:codelist5">15</meta>
                </metadata>
            </package>
            """;

        BookOpf result = XmlUtils.parse(new ByteArrayInputStream(xml.getBytes()));

        assertNotNull(result);
        assertEquals(List.of("9780306406157"), result.getIsbn13());
    }

    @Test
    void parse_WithCoverMetadataId_ShouldResolveManifestImageHref() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata><meta name="cover" content="cover-1"/></metadata>
                <manifest><item id="cover-1" href="Images/cover.jpg" media-type="image/jpeg"/></manifest>
            </package>
            """;

        BookOpf result = XmlUtils.parse(new ByteArrayInputStream(xml.getBytes()));

        assertNotNull(result);
        assertEquals("Images/cover.jpg", result.getBookImageName());
    }

    @Test
    void parse_WithEpub3CoverProperty_ShouldResolveManifestImageHref() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf">
                <metadata/>
                <manifest><item id="image" href="Images/front.jpeg" properties="nav cover-image"/></manifest>
            </package>
            """;

        BookOpf result = XmlUtils.parse(new ByteArrayInputStream(xml.getBytes()));

        assertNotNull(result);
        assertEquals("Images/front.jpeg", result.getBookImageName());
    }

    @Test
    void parse_WithInvalidXml_ShouldReturnNull() {
        // Given
        String invalidXml = "This is not valid XML";
        InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes());

        // When
        BookOpf result = XmlUtils.parse(inputStream);

        // Then
        assertNull(result);
    }

    @Test
    void getNumber_WithValidInteger_ShouldReturnDouble() throws Exception {
        // Given
        String content = "42";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNotNull(result);
        assertEquals(42.0, result);
    }

    @Test
    void getNumber_WithValidDecimal_ShouldReturnDouble() throws Exception {
        // Given
        String content = "3.14";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNotNull(result);
        assertEquals(3.14, result);
    }

    @Test
    void getNumber_WithNegativeNumber_ShouldReturnNegativeDouble() throws Exception {
        // Given
        String content = "-25.5";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNotNull(result);
        assertEquals(-25.5, result);
    }

    @Test
    void getNumber_WithCommaAsDecimalSeparator_ShouldReturnDouble() throws Exception {
        // Given
        String content = "12,75";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNotNull(result);
        assertEquals(12.0, result); // The regex only matches the first number "12"
    }

    @Test
    void getNumber_WithEmptyString_ShouldReturnNull() throws Exception {
        // Given
        String content = "";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNull(result);
    }

    @Test
    void getNumber_WithNullString_ShouldReturnNull() throws Exception {
        // Given
        String content = null;
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNull(result);
    }

    @Test
    void getNumber_WithNonNumericString_ShouldReturnNull() throws Exception {
        // Given
        String content = "not a number";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNull(result);
    }

    @Test
    void getNumber_WithMixedContent_ShouldReturnFirstNumber() throws Exception {
        // Given
        String content = "Price: 19.99 euros";
        Method method = XmlUtils.class.getDeclaredMethod("getNumber", String.class);
        method.setAccessible(true);

        // When
        Double result = (Double) method.invoke(null, content);

        // Then
        assertNotNull(result);
        assertEquals(19.99, result);
    }

    @Test
    void extractAttributeValue_WithValidAttribute_ShouldReturnValue() throws Exception {
        // Given
        String content = "{\"#value#\": \"test_value\", \"other\": \"other_value\"}";
        String attributeName = "#value#";
        Method method = XmlUtils.class.getDeclaredMethod("extractAttributeValue", String.class, String.class);
        method.setAccessible(true);

        // When
        String result = (String) method.invoke(null, content, attributeName);

        // Then
        assertNotNull(result);
        assertEquals("\"test_value\"", result);
    }

    @Test
    void extractAttributeValue_WithNonExistentAttribute_ShouldReturnNull() throws Exception {
        // Given
        String content = "{\"#value#\": \"test_value\"}";
        String attributeName = "#missing#";
        Method method = XmlUtils.class.getDeclaredMethod("extractAttributeValue", String.class, String.class);
        method.setAccessible(true);

        // When
        String result = (String) method.invoke(null, content, attributeName);

        // Then
        assertNull(result);
    }

    @Test
    void addAAVVifNotExist_WithMultipleAuthorsWithoutAAVV_ShouldAddAAVV() throws Exception {
        // Given
        java.util.List<String> authors = new java.util.ArrayList<>();
        authors.add("Author One");
        authors.add("Author Two");
        Method method = XmlUtils.class.getDeclaredMethod("addAAVVifNotExist", List.class);
        method.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(null, authors);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("AA. VV."));
    }

    @Test
    void addAAVVifNotExist_WithMultipleAuthorsWithAAVV_ShouldNotAddAAVV() throws Exception {
        // Given
        java.util.List<String> authors = new java.util.ArrayList<>();
        authors.add("Author One");
        authors.add("AA. VV.");
        Method method = XmlUtils.class.getDeclaredMethod("addAAVVifNotExist", List.class);
        method.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(null, authors);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        long aavvCount = result.stream().filter(author -> author.equals("AA. VV.")).count();
        assertEquals(1, aavvCount);
    }

    @Test
    void addAAVVifNotExist_WithSingleAuthor_ShouldNotAddAAVV() throws Exception {
        // Given
        java.util.List<String> authors = new java.util.ArrayList<>();
        authors.add("Single Author");
        Method method = XmlUtils.class.getDeclaredMethod("addAAVVifNotExist", List.class);
        method.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(null, authors);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.contains("AA. VV."));
    }

    @Test
    void addAAVVifNotExist_WithEmptyList_ShouldReturnEmptyList() throws Exception {
        // Given
        java.util.List<String> authors = new java.util.ArrayList<>();
        Method method = XmlUtils.class.getDeclaredMethod("addAAVVifNotExist", List.class);
        method.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(null, authors);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
