package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlArticle;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class FindGoodReadsReviewsAdapterIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private FindGoodReadsReviewsPort findGoodReadsReviewsPort;

	@SneakyThrows
	@BeforeEach
	public void init() {
		final HtmlPage page = Mockito.mock(HtmlPage.class);
		final HtmlAnchor htmlAnchor = Mockito.mock(HtmlAnchor.class);
		List list = new ArrayList();
		list.add(htmlAnchor);
		final DomNode child1 = Mockito.mock(DomNode.class);
		final DomNode child2 = Mockito.mock(DomNode.class);
		final DomNode child3 = Mockito.mock(DomNode.class);
		Mockito.when(htmlAnchor.getAttribute("href")).thenReturn("ref");
		Mockito.when(htmlAnchor.getHrefAttribute()).thenReturn("/ref");
		Mockito.when(htmlAnchor.getFirstChild()).thenReturn(child1);
		Mockito.when(child1.getNextSibling()).thenReturn(child2);
		Mockito.when(child2.getFirstChild()).thenReturn(child3);
		Mockito.when(child3.asText()).thenReturn("title");
		Mockito.when(page.getByXPath(Mockito.anyString())).thenReturn(list);
		Mockito.when(webClient.getPage("https://www.goodreads.com/search?q=example_title+author1+author2&search_type=books"))
				.thenReturn(page);

		final HtmlArticle htmlArticle = Mockito.mock(HtmlArticle.class);
		List list2 = new ArrayList();
		list2.add(htmlArticle);
		final DomNode child21 = Mockito.mock(DomNode.class);
		final DomNode child22 = Mockito.mock(DomNode.class);
		final DomNode child23 = Mockito.mock(DomNode.class);
		final DomNode child24 = Mockito.mock(DomNode.class);
		final DomNode child25 = Mockito.mock(DomNode.class);
		final DomNode child26 = Mockito.mock(DomNode.class);
		final DomNode child27 = Mockito.mock(DomNode.class);
		final DomNode child28 = Mockito.mock(DomNode.class);
		final NamedNodeMap attributes = Mockito.mock(NamedNodeMap.class);
		final Node node = Mockito.mock(Node.class);
		final HtmlPage page2 = Mockito.mock(HtmlPage.class);
		Mockito.when(htmlArticle.getFirstChild()).thenReturn(child21);
		Mockito.when(child21.getFirstChild()).thenReturn(child22);
		Mockito.when(child21.getNextSibling()).thenReturn(child22);
		Mockito.when(child22.getFirstChild()).thenReturn(child23);
		Mockito.when(child23.getNextSibling()).thenReturn(child24);
		Mockito.when(child23.getFirstChild()).thenReturn(child24);
		Mockito.when(child24.getFirstChild()).thenReturn(child25);
		Mockito.when(child24.getNextSibling()).thenReturn(child25);
		Mockito.when(child25.getFirstChild()).thenReturn(child26);
		Mockito.when(child25.asText()).thenReturn("january 23, 2014");
		Mockito.when(child25.getAttributes()).thenReturn(attributes);
		Mockito.when(attributes.getNamedItem(anyString())).thenReturn(node);
		Mockito.when(node.getNodeValue()).thenReturn("5.0");
		Mockito.when(child26.getFirstChild()).thenReturn(child27);
		Mockito.when(child27.getFirstChild()).thenReturn(child28);
		Mockito.when(child27.asText()).thenReturn("name");
		Mockito.when(child28.asText()).thenReturn("comment");
		Mockito.when(page2.getByXPath(Mockito.anyString())).thenReturn(list2);
		Mockito.when(webClient.getPage("https://www.goodreads.com/ref")).thenReturn(page2);

		doReturn("en").when(detectLibreTranslatePort).detect(anyString());
		doReturn("comment").when(translateLibreTranslatePort).translate(anyString(), anyString());
	}

	@Test
	public void findGoodReadsReviewsES() {
		// Given
		String lang = "es";
		String title = "example_title";
		List<String> authors = Arrays.asList("author1", "author2");

		// When
		List<ReviewDto> results = findGoodReadsReviewsPort.getReviews(lang, title, authors);

		// Then
		assertNotNull(results);
		assertEquals("name", results.get(0).getName());
		assertEquals("", results.get(0).getTitle());
		assertEquals("comment", results.get(0).getComment());
		assertEquals(5, results.get(0).getRating());
	}

	@Test
	public void findGoodReadsReviewsEN() {
		// Given
		String lang = "en";
		String title = "example_title";
		List<String> authors = Arrays.asList("author1", "author2");

		// When
		List<ReviewDto> results = findGoodReadsReviewsPort.getReviews(lang, title, authors);

		// Then
		assertNotNull(results);
		assertEquals("name", results.get(0).getName());
		assertEquals("", results.get(0).getTitle());
		assertEquals("comment", results.get(0).getComment());
		assertEquals(5, results.get(0).getRating());
	}
}
