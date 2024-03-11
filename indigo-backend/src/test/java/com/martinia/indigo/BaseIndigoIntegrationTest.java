package com.martinia.indigo;

import com.gargoylesoftware.htmlunit.WebClient;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsAuthorPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.infrastructure.events.BookMetadataFoundFindBookRecommendationsEventListener;
import com.martinia.indigo.metadata.infrastructure.events.BookMetadataFoundFindSimilarBooksEventListener;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

public class BaseIndigoIntegrationTest extends BaseIndigoTest {

	@Resource
	protected MockMvc mockMvc;

	@Resource
	protected EventBus eventBus;

	@Resource
	protected CommandBus commandBus;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@SpyBean
	protected BookMetadataFoundFindBookRecommendationsEventListener bookMetadataFoundFindBookRecommendationsEventListener;

	@SpyBean
	protected BookMetadataFoundFindSimilarBooksEventListener bookMetadataFoundFindSimilarBooksEventListener;

	@SpyBean
	protected FindWikipediaAuthorPort findWikipediaAuthorPort;

	@SpyBean
	protected FindGoodReadsAuthorPort findGoodReadsAuthorPort;

	@SpyBean
	protected FindGoodReadsBookPort findGoodReadsBookPort;

	@SpyBean
	protected FindGoogleBooksBookPort findGoogleBooksBookPort;

	@SpyBean
	protected FindGoodReadsReviewsPort findGoodReadsReviewsPort;

	@SpyBean
	protected FindAmazonReviewsPort findAmazonReviewsPort;

	@SpyBean
	protected DetectLibreTranslatePort detectLibreTranslatePort;

	@SpyBean
	protected TranslateLibreTranslatePort translateLibreTranslatePort;

	@SpyBean
	protected FindWikipediaAuthorInfoPort findWikipediaAuthorInfoPort;

	@MockBean
	protected DataUtils dataUtils;

	@MockBean
	protected WebClient webClient;

	@MockBean
	protected RestTemplate restTemplate;
}
