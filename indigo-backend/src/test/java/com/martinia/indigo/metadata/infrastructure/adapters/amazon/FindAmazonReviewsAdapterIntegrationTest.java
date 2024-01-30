package com.martinia.indigo.metadata.infrastructure.adapters.amazon;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class FindAmazonReviewsAdapterIntegrationTest extends BaseIndigoIntegrationTest {

	public static final String COMMENT = "comment";
	public static final String DATE = "5 enero 2014";
	public static final String RATING = "5";
	public static final String NAME = "name";
	public static final String TITLE = "compareAsin";
	@Resource
	private FindAmazonReviewsPort findAmazonReviewsPort;


	@BeforeEach
	@SneakyThrows
	public void init() {


		HtmlPage page = Mockito.mock(HtmlPage.class);
		HtmlDivision htmlDivision = Mockito.mock(HtmlDivision.class);
		Mockito.when(htmlDivision.getAttribute(anyString())).thenReturn(TITLE);
		final DomNode child1 = Mockito.mock(DomNode.class);
		final DomNode child2 = Mockito.mock(DomNode.class);
		final DomNode child3 = Mockito.mock(DomNode.class);
		final DomNode child4 = Mockito.mock(DomNode.class);
		final DomNode child5 = Mockito.mock(DomNode.class);
		final DomNode child6 = Mockito.mock(DomNode.class);
		final DomNode child7 = Mockito.mock(DomNode.class);
		final DomNode child8 = Mockito.mock(DomNode.class);
		final DomNode child9 = Mockito.mock(DomNode.class);
		final DomNode child10 = Mockito.mock(DomNode.class);
		final DomNode child11 = Mockito.mock(DomNode.class);
		final DomNode child12 = Mockito.mock(DomNode.class);
		final DomNode child13 = Mockito.mock(DomNode.class);
		Mockito.when(child1.getFirstChild()).thenReturn(child2);
		Mockito.when(child2.getFirstChild()).thenReturn(child3);
		Mockito.when(child3.getFirstChild()).thenReturn(child4);
		Mockito.when(child3.getNextSibling()).thenReturn(child4);
		Mockito.when(child4.getFirstChild()).thenReturn(child5);
		Mockito.when(child4.getNextSibling()).thenReturn(child7);
		Mockito.when(child5.getFirstChild()).thenReturn(child6);
		Mockito.when(child6.getNextSibling()).thenReturn(child7);
		Mockito.when(child6.asText()).thenReturn(RATING);
		Mockito.when(child5.getNextSibling()).thenReturn(child8);
		Mockito.when(child7.getFirstChild()).thenReturn(child8);
		Mockito.when(child7.getNextSibling()).thenReturn(child8);
		Mockito.when(child8.getFirstChild()).thenReturn(child9);
		Mockito.when(child8.asText()).thenReturn(DATE);
		Mockito.when(child8.getNextSibling()).thenReturn(child9);
		Mockito.when(child9.asText()).thenReturn(NAME);
		Mockito.when(child9.getFirstChild()).thenReturn(child10);
		Mockito.when(child10.getFirstChild()).thenReturn(child11);
		Mockito.when(child10.getNextSibling()).thenReturn(child11);
		Mockito.when(child11.getFirstChild()).thenReturn(child12);
		Mockito.when(child11.getNextSibling()).thenReturn(child12);
		Mockito.when(child12.asText()).thenReturn(TITLE);
		Mockito.when(child12.getFirstChild()).thenReturn(child13);
		Mockito.when(child13.asText()).thenReturn(COMMENT);
		Mockito.when(htmlDivision.getFirstChild()).thenReturn(child1);
		List list = new ArrayList<>();
		list.add(htmlDivision);
		Mockito.when(page.getByXPath(anyString())).thenReturn(list);
		Mockito.when(webClient.getPage(Mockito.anyString())).thenReturn(page);
	}

	@Test
	@SneakyThrows
	void findAmazonReviews() {
		//Given
		final String title = "title <compareAsin>";
		final List<String> authors = List.of("author");

		//When
		List<ReviewDto> reviewDtos = findAmazonReviewsPort.getReviews(title, authors);

		//Then
		assertEquals(1, reviewDtos.size());
		assertEquals(NAME, reviewDtos.get(0).getName());
		assertEquals(TITLE, reviewDtos.get(0).getTitle());
		assertEquals(RATING, String.valueOf(reviewDtos.get(0).getRating()));
		assertEquals(new SimpleDateFormat("d MMMM yyyy").parse(DATE).toString(), reviewDtos.get(0).getDate().toString());
		assertEquals(COMMENT, reviewDtos.get(0).getComment());

	}

}