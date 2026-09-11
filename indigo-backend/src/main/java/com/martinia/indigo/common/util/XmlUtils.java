package com.martinia.indigo.common.util;

import com.martinia.indigo.common.domain.model.BookOpf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class XmlUtils {
	public static BookOpf parse(InputStream is) {
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);

			Element root = document.getDocumentElement();

			root.setAttribute("xmlns", "http://www.idpf.org/2007/opf");

			// Obtener los elementos necesarios
			final String title = getElementValue(root, "dc:title");
			final String description = getElementValue(root, "dc:description");
			final List<String> creators = getCreators(root);
			final String creatorId = getAttribute(root, "dc:creator", "opf:file-as");
			final List<String> translators = getTranslators(root);
			final String language = getElementValue(root, "dc:language");
			Date pubDate = Optional.ofNullable(getDateValue(root, "dc:date", "publication"))
					.filter(StringUtils::isNoneEmpty)
					.map(DateUtils::parseDate)
					.orElse(null);
			final Date lastModified = Optional.ofNullable(getDateValue(root, "dc:date", "modification"))
					.filter(StringUtils::isNoneEmpty)
					.map(DateUtils::parseDate)
					.orElse(null);
			if (pubDate == null && lastModified != null) {
				pubDate = lastModified;
			}
			final String seriesName = Optional.ofNullable(getMetaContent(root, "calibre:series"))
					.filter(StringUtils::isNoneEmpty)
					.orElse(null);
			int seriesIndex = Optional.ofNullable(getMetaContent(root, "calibre:series_index"))
					.filter(StringUtils::isNoneEmpty)
					.map(XmlUtils::getNumber)
					.map(index -> (int) Math.floor(index))
					.orElse(0);
			final List<String> subjects = getSubjects(root);
			final Map<String, List<String>> identifiers = getIdentifiers(root);
			final List<String> isbn10 = identifiers.getOrDefault("ISBN_10", Collections.emptyList());
			final List<String> isbn13 = identifiers.getOrDefault("ISBN_13", Collections.emptyList());
			int pages = Optional.ofNullable(getMetaAttributeValue(root, "calibre:user_metadata:#pages", "#value#"))
					.filter(StringUtils::isNoneEmpty)
					.filter(item -> !item.equals("null"))
					.map(Integer::valueOf)
					.orElse(0);

			if (pages==0){
				pages = Optional.ofNullable(getMetaAttributeValue(root, "calibre:user_metadata:#paginas", "#value#"))
						.filter(StringUtils::isNoneEmpty)
						.filter(item -> !item.equals("null"))
						.map(Integer::valueOf)
						.orElse(0);
			}

			final float version = Optional.ofNullable(getMetaAttributeValue(root, "calibre:user_metadata:#version", "#value#"))
					.filter(StringUtils::isNoneEmpty)
					.map(vrs -> vrs.replace("[", "").replace("]", "").replace("'", "").replace("\"", ""))
					.map(XmlUtils::parseVersion)
					.orElse(0F);
			final String bookImageName = getManifestItemHref(root, getMetaContent(root, "cover"), "cover-image");
			final String authorImageName = getMetaContent(root, "autor");

			final BookOpf bookOpf = BookOpf.builder()
					.title(title)
					.comment(description)
					.authors(creators)
					.authorId(creatorId)
					.translators(translators)
					.tags(subjects)
					.isbn10(isbn10)
					.isbn13(isbn13)
					.identifiers(identifiers)
					.language(language)
					.pubDate(pubDate)
					.lastModified(lastModified)
					.seriesName(seriesName)
					.seriesIndex(seriesIndex)
					.pages(pages)
					.version(version)
					.bookImageName(bookImageName)
					.authorImageName(authorImageName)
					.build();

			return bookOpf;

		}
		catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private static float parseVersion(String value) {
		try {
			float version = Float.parseFloat(value.trim());
			return Float.isFinite(version) && version > 0 ? version : 0F;
		} catch (NumberFormatException exception) { return 0F; }
	}

	private static String getAttribute(final Element parent, final String name, final String attribute) {
		return IntStream.range(0, parent.getElementsByTagName(name).getLength())
				.mapToObj(i -> (Element) parent.getElementsByTagName(name).item(i))
				.filter(creatorElement -> creatorElement.hasAttribute(attribute))
				.map(creatorElement -> creatorElement.getAttribute(attribute))
				.findFirst()
				.filter(StringUtils::isNoneEmpty)
				.orElse(null);
	}

	private static String getManifestItemHref(final Element parent, final String itemId, final String property) {
		String propertyHref = null;
		final NodeList elements = parent.getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); i++) {
			final Node node = elements.item(i);
			final String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
			if (!(node instanceof Element item) || (!nodeName.equals("item") && !nodeName.endsWith(":item"))) {
				continue;
			}
			final String href = item.getAttribute("href");
			if (StringUtils.isEmpty(href)) {
				continue;
			}
			if (StringUtils.isNotEmpty(itemId) && itemId.equals(item.getAttribute("id"))) {
				return href;
			}
			if (Arrays.asList(item.getAttribute("properties").split("\\s+")).contains(property)) {
				propertyHref = href;
			}
		}
		return Optional.ofNullable(propertyHref).orElse(itemId);
	}

	private static List<String> getSubjects(Element parent) {
		final Map<String, String> subjects = new LinkedHashMap<>();
		final NodeList elements = parent.getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); i++) {
			final Node node = elements.item(i);
			final String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
			if (!nodeName.equals("subject") && !nodeName.endsWith(":subject")) {
				continue;
			}
			for (String value : node.getTextContent().split("[,;]")) {
				final String subject = value.trim();
				if (StringUtils.isNotEmpty(subject)) {
					subjects.putIfAbsent(subject.toLowerCase(Locale.ROOT), subject);
				}
			}
		}
		return new ArrayList<>(subjects.values());
	}

	private static Map<String, List<String>> getIdentifiers(final Element parent) {
		final Map<String, String> refinedTypes = getRefinedIdentifierTypes(parent);
		final Map<String, List<String>> identifiers = new LinkedHashMap<>();
		final NodeList elements = parent.getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); i++) {
			final Node node = elements.item(i);
			final String nodeName = node.getNodeName().toLowerCase(Locale.ROOT);
			if (!(node instanceof Element identifier)
					|| (!nodeName.equals("identifier") && !nodeName.endsWith(":identifier"))) {
				continue;
			}

			final String rawValue = StringUtils.trimToNull(identifier.getTextContent());
			if (rawValue == null) {
				continue;
			}
			String scheme = StringUtils.firstNonBlank(identifier.getAttribute("opf:scheme"),
					identifier.getAttribute("scheme"));
			if (StringUtils.isBlank(scheme) && StringUtils.isNotBlank(identifier.getAttribute("id"))) {
				scheme = refinedTypes.get(identifier.getAttribute("id"));
			}
			addIdentifier(identifiers, scheme, rawValue);
		}
		return identifiers;
	}

	private static Map<String, String> getRefinedIdentifierTypes(final Element parent) {
		final Map<String, String> types = new LinkedHashMap<>();
		final NodeList elements = parent.getElementsByTagName("*");
		for (int i = 0; i < elements.getLength(); i++) {
			if (!(elements.item(i) instanceof Element meta)
					|| !"identifier-type".equalsIgnoreCase(meta.getAttribute("property"))) {
				continue;
			}
			final String target = StringUtils.removeStart(meta.getAttribute("refines"), "#");
			final String value = StringUtils.trimToNull(meta.getTextContent());
			if (StringUtils.isNotBlank(target) && value != null) {
				types.put(target, switch (value) {
					case "02" -> "ISBN_10";
					case "15" -> "ISBN_13";
					default -> value;
				});
			}
		}
		return types;
	}

	private static void addIdentifier(final Map<String, List<String>> identifiers, final String declaredScheme,
			final String rawValue) {
		final String normalizedIsbn = normalizeIsbn(rawValue);
		final boolean isbnCandidate = StringUtils.containsIgnoreCase(declaredScheme, "isbn")
				|| StringUtils.startsWithIgnoreCase(rawValue, "urn:isbn:")
				|| rawValue.matches("(?i)[0-9X\\-\\s]+?");
		if (isbnCandidate && normalizedIsbn.length() == 10 && isValidIsbn10(normalizedIsbn)) {
			addIdentifierValue(identifiers, "ISBN_10", normalizedIsbn);
			return;
		}
		if (isbnCandidate && normalizedIsbn.length() == 13 && isValidIsbn13(normalizedIsbn)) {
			addIdentifierValue(identifiers, "ISBN_13", normalizedIsbn);
			return;
		}

		String scheme = StringUtils.trimToNull(declaredScheme);
		String value = rawValue.trim();
		final int urnSeparator = value.indexOf(':');
		if (scheme == null && value.regionMatches(true, 0, "urn:", 0, 4) && urnSeparator >= 0) {
			final int valueSeparator = value.indexOf(':', urnSeparator + 1);
			if (valueSeparator > urnSeparator) {
				scheme = value.substring(urnSeparator + 1, valueSeparator);
				value = value.substring(valueSeparator + 1);
			}
		}
		addIdentifierValue(identifiers, normalizeIdentifierScheme(scheme), value);
	}

	private static void addIdentifierValue(final Map<String, List<String>> identifiers, final String scheme,
			final String value) {
		final List<String> values = identifiers.computeIfAbsent(scheme, ignored -> new ArrayList<>());
		if (values.stream().noneMatch(value::equalsIgnoreCase)) {
			values.add(value);
		}
	}

	private static String normalizeIdentifierScheme(final String scheme) {
		if (StringUtils.isBlank(scheme)) {
			return "OTHER";
		}
		final String normalized = scheme.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_")
				.replaceAll("^_+|_+$", "");
		return normalized.isEmpty() ? "OTHER" : normalized;
	}

	private static String normalizeIsbn(final String value) {
		String candidate = value.trim();
		if (candidate.regionMatches(true, 0, "urn:isbn:", 0, 9)) {
			candidate = candidate.substring(9);
		}
		return candidate.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
	}

	private static boolean isValidIsbn10(final String isbn) {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			final char character = isbn.charAt(i);
			if (character == 'X' && i != 9 || character != 'X' && !Character.isDigit(character)) {
				return false;
			}
			final int digit = character == 'X' ? 10 : Character.digit(character, 10);
			sum += (10 - i) * digit;
		}
		return sum % 11 == 0;
	}

	private static boolean isValidIsbn13(final String isbn) {
		int sum = 0;
		for (int i = 0; i < 13; i++) {
			if (!Character.isDigit(isbn.charAt(i))) {
				return false;
			}
			sum += Character.digit(isbn.charAt(i), 10) * (i % 2 == 0 ? 1 : 3);
		}
		return sum % 10 == 0;
	}

	private static List<String> getCreators(Element parent) {
		String creatorValue = getElementValue(parent, "dc:creator");
		if (StringUtils.isEmpty(creatorValue)) {
			return Collections.emptyList();
		}

		String[] creatorArray = creatorValue.split("&");
		List<String> authors = Arrays.stream(creatorArray)
				.map(String::trim)
				.filter(creator -> creatorArray.length == 1 || !creator.equalsIgnoreCase("AA. VV."))
				.collect(Collectors.toList());

		return addAAVVifNotExist(authors);
	}

	private static List<String> addAAVVifNotExist(final List<String> authors) {
		if (!CollectionUtils.isEmpty(authors) && authors.size() > 1 && authors.stream()
				.filter(author -> author.toUpperCase().contains("AA"))
				.filter(author -> author.toUpperCase().contains("VV"))
				.findAny()
				.isEmpty()) {
			authors.add("AA. VV.");
		}
		return authors;
	}

	private static List<String> getTranslators(Element parent) {
		NodeList contributorList = parent.getElementsByTagName("dc:contributor");

		return IntStream.range(0, contributorList.getLength())
				.mapToObj(i -> (Element) contributorList.item(i))
				.filter(contributorElement -> contributorElement.hasAttribute("opf:role") && contributorElement.getAttribute("opf:role")
						.equals("trl"))
				.map(Element::getTextContent)
				.filter(StringUtils::isNotEmpty)
				.flatMap(translatorValue -> Arrays.stream(translatorValue.split("&")))
				.map(String::trim)
				.collect(Collectors.toList());
	}

	private static String getElementValue(Element parent, String elementName) {
		NodeList nodeList = parent.getElementsByTagName(elementName);
		if (nodeList.getLength() > 0) {
			Node node = nodeList.item(0);
			return node.getTextContent();
		}
		return null;
	}

	private static String getDateValue(Element parent, String elementName, String eventAttribute) {
		NodeList nodeList = parent.getElementsByTagName(elementName);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getAttributes().getNamedItem("opf:event") != null && node.getAttributes()
					.getNamedItem("opf:event")
					.getNodeValue()
					.equals(eventAttribute)) {
				return node.getTextContent();
			} else if (node.getFirstChild()!=null){
				return node.getFirstChild().getNodeValue();
			}
		}
		return null;
	}

	private static String getMetaContent(Element parent, String metaName) {
		NodeList metaList = parent.getElementsByTagName("meta");
		for (int i = 0; i < metaList.getLength(); i++) {
			Node metaNode = metaList.item(i);
			Element metaElement = (Element) metaNode;
			if (metaElement.getAttribute("name").equals(metaName)) {
				return metaElement.getAttribute("content");
			}
		}
		return null;
	}

	private static String getMetaAttributeValue(Element parent, String name, String attributeName) {
		NodeList metaNodes = parent.getElementsByTagName("meta");

		return IntStream.range(0, metaNodes.getLength())
				.mapToObj(metaNodes::item)
				.filter(node -> node instanceof Element)
				.map(node -> (Element) node)
				.filter(metaElement -> metaElement.getAttribute("name").equals(name))
				.map(metaElement -> metaElement.getAttribute("content"))
				.map(content -> extractAttributeValue(content, attributeName))
				.findFirst()
				.orElse(null);
	}

	private static String extractAttributeValue(String content, String attributeName) {

		String escapedAttributeName = Pattern.quote(attributeName);
		Pattern pattern = Pattern.compile("\"" + escapedAttributeName + "\":\\s*([^,]*)");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		}
		else {
			return null;
		}

	}

	private static Double getNumber(final String content) {

		if (StringUtils.isEmpty(content)) {
			return null;
		}

		String regex = "-?\\d*\\.?\\d+";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) {
			String numberStr = matcher.group();
			return Double.parseDouble(numberStr.replace(",", "."));
		}

		return null;
	}

}
