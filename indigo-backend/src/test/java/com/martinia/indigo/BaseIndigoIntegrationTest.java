package com.martinia.indigo;

import com.martinia.indigo.adapters.out.sqlite.repository.AuthorSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.repository.BookSqliteRepository;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.infrastructure.events.BookMetadataFoundFindBookRecommendationsEventListener;
import com.martinia.indigo.metadata.infrastructure.events.BookMetadataFoundFindSimilarBooksEventListener;
import com.martinia.indigo.metadata.infrastructure.events.FillAuthorsBookLoadedEventListener;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

public class BaseIndigoIntegrationTest extends BaseIndigoTest {

	@Resource
	protected MockMvc mockMvc;

	@Resource
	protected EventBus eventBus;

	@Resource
	protected MetadataSingleton metadataSingleton;

	@SpyBean
	protected BookMetadataFoundFindBookRecommendationsEventListener bookMetadataFoundFindBookRecommendationsEventListener;

	@SpyBean
	protected BookMetadataFoundFindSimilarBooksEventListener bookMetadataFoundFindSimilarBooksEventListener;

	@SpyBean
	protected FillAuthorsBookLoadedEventListener fillAuthorsBookLoadedEventListener;

	@MockBean
	protected BookSqliteRepository bookSqliteRepository;

	@MockBean
	protected AuthorSqliteRepository authorSqliteRepository;

}
