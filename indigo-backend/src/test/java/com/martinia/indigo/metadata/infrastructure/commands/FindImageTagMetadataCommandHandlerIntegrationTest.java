package com.martinia.indigo.metadata.infrastructure.commands;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.metadata.domain.model.commands.FindImageTagMetadataCommand;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

class FindImageTagMetadataCommandHandlerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	private WebClient webClient;

	@Test
	@SneakyThrows
	public void testHandle() {
		// Given
		FindImageTagMetadataCommand command = FindImageTagMetadataCommand.builder().tag("tagToFind").build();

		HtmlPage page = Mockito.mock(HtmlPage.class);
		HtmlDivision htmlDivision = Mockito.mock(HtmlDivision.class);
		final HtmlImage domNode = Mockito.mock(HtmlImage.class);
		Mockito.when(domNode.getAttribute(anyString())).thenReturn("image");
		Mockito.when(htmlDivision.getFirstChild()).thenReturn(domNode);
		List list = new ArrayList<>();
		list.add(htmlDivision);
		Mockito.when(page.getByXPath(anyString())).thenReturn(list);
		Mockito.when(webClient.getPage(Mockito.anyString())).thenReturn(page);

		// When
		String image = commandBus.executeAndWait(command);

		// Then
		assertEquals("image", image);
	}

}