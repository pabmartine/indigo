package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorInfoUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindWikipediaAuthorInfoAdapterIntegrationTest extends BaseIndigoIntegrationTest {

	public static final String JSON = "{\"batchcomplete\":\"\",\"continue\":{\"gsroffset\":1,\"continue\":\"gsroffset||\"},\"query\":{\"pages\":{\"831554\":{\"pageid\":831554,\"ns\":0,\"title\":\"Alfonso Carrillo de Acu\\u00f1a\",\"index\":1,\"extract\":\"Alfonso (o Alonso) Carrillo de Acu\\u00f1a (Carrascosa del Campo, 11 de agosto de 1413-Alcal\\u00e1 de Henares, 1 de julio de 1482). Importante prelado de la Castilla del siglo XV.\",\"original\":{\"source\":\"https://upload.wikimedia.org/wikipedia/commons/f/f7/Alonso_Carrillo_de_Acu%C3%B1a_1508.gif\",\"width\":750,\"height\":1009}}}}}";
	@Resource
	private FindWikipediaAuthorInfoPort findWikipediaAuthorInfoPort;

	@Test
	void FindWikipediaAuthorInfoOK() {
		//Given
		String subject = "Alfonso Carrillo de Acuña";
		String lang = "es";
		String[] expectedAuthorInfo = { "Alfonso (o Alonso) Carrillo de Acuña (Carrascosa del Campo, 11 de agosto de 1413-Alcalá de Henares, 1 de julio de 1482). Importante prelado de la Castilla del siglo XV.", "https://upload.wikimedia.org/wikipedia/commons/f/f7/Alonso_Carrillo_de_Acu%C3%B1a_1508.gif",
				ProviderEnum.WIKIPEDIA.name() };

		Mockito.when(dataUtils.getData(anyString())).thenReturn(JSON);
		//When
		String[] authorInfo = findWikipediaAuthorInfoPort.getAuthorInfo(subject, lang);

		//Then
		assertArrayEquals(expectedAuthorInfo, authorInfo);

	}
}
