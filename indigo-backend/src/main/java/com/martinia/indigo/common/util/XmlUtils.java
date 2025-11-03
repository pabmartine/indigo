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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
					.map(Float::parseFloat)
					.orElse(0F);
			final String bookImageName = getMetaContent(root, "cover");
			final String authorImageName = getMetaContent(root, "autor");

			final BookOpf bookOpf = BookOpf.builder()
					.title(title)
					.comment(description)
					.authors(creators)
					.authorId(creatorId)
					.translators(translators)
					.tags(subjects)
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

	private static String getAttribute(final Element parent, final String name, final String attribute) {
		return IntStream.range(0, parent.getElementsByTagName(name).getLength())
				.mapToObj(i -> (Element) parent.getElementsByTagName(name).item(i))
				.filter(creatorElement -> creatorElement.hasAttribute(attribute))
				.map(creatorElement -> creatorElement.getAttribute(attribute))
				.findFirst()
				.filter(StringUtils::isNoneEmpty)
				.orElse(null);
	}

	private static List<String> getSubjects(Element parent) {
		String subjectValue = getElementValue(parent, "dc:subject");
		if (StringUtils.isEmpty(subjectValue)) {
			return Collections.emptyList();
		}

		return Arrays.stream(subjectValue.split(",")).map(String::trim).collect(Collectors.toList());
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

