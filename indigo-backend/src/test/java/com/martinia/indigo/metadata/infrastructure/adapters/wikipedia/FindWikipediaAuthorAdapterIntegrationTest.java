package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.ProviderEnum;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorPort;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FindWikipediaAuthorAdapterIntegrationTest extends BaseIndigoIntegrationTest {

	public static final String JSON = "{\"batchcomplete\":\"\",\"continue\":{\"sroffset\":10,\"continue\":\"-||\"},\"query\":{\"searchinfo\":{\"totalhits\":21},\"search\":[{\"ns\":0,\"title\":\"Alfonso Carrillo de Acuña\",\"pageid\":831554,\"size\":12296,\"wordcount\":1127,\"snippet\":\"Alfonso (o <span class=\\\"searchmatch\\\">Alonso</span>) Carrillo <span class=\\\"searchmatch\\\">de</span> Acuña (Carrascosa del Campo, 11 <span class=\\\"searchmatch\\\">de</span> agosto <span class=\\\"searchmatch\\\">de</span> 1413-Alcalá <span class=\\\"searchmatch\\\">de</span> Henares, 1 <span class=\\\"searchmatch\\\">de</span> julio <span class=\\\"searchmatch\\\">de</span> 1482). Importante prelado <span class=\\\"searchmatch\\\">de</span> la Castilla\",\"timestamp\":\"2024-01-02T15:54:02Z\"},{\"ns\":0,\"title\":\"Alfonso Carrillo de Acuña (señor)\",\"pageid\":1377716,\"size\":9559,\"wordcount\":1169,\"snippet\":\"rey Pedro I <span class=\\\"searchmatch\\\">de</span> Castilla.[1]\u200B <span class=\\\"searchmatch\\\">Alonso</span> Carrillo <span class=\\\"searchmatch\\\">de</span> Acuña fue hijo primogénito <span class=\\\"searchmatch\\\">de</span> Gómez Carrillo <span class=\\\"searchmatch\\\">de</span> Acuña, camarero mayor del rey <span class=\\\"searchmatch\\\">Juan</span> II <span class=\\\"searchmatch\\\">de</span> Castilla, miembro\",\"timestamp\":\"2023-12-25T13:47:29Z\"},{\"ns\":0,\"title\":\"Gómez Carrillo de Acuña\",\"pageid\":5509370,\"size\":8007,\"wordcount\":844,\"snippet\":\"descendencia:[9]\u200B <span class=\\\"searchmatch\\\">Alonso</span> Carrillo <span class=\\\"searchmatch\\\">de</span> Acuña, quien heredó el señorío y el Castillo <span class=\\\"searchmatch\\\">de</span> Jadraque. Leonor Carrillo <span class=\\\"searchmatch\\\">de</span> Acuña, casada con Alvar Pérez <span class=\\\"searchmatch\\\">de</span> Guzmán el Mozo\",\"timestamp\":\"2023-12-26T19:06:42Z\"},{\"ns\":0,\"title\":\"Hernando de Acuña\",\"pageid\":102123,\"size\":9237,\"wordcount\":1154,\"snippet\":\"de Eduardo Marquina, es capitán Diego <span class=\\\"searchmatch\\\">de</span> Acuña y no Hernando <span class=\\\"searchmatch\\\">de</span> Acuña. <span class=\\\"searchmatch\\\">Alonso</span> Cortés, Narciso: Don Hernando <span class=\\\"searchmatch\\\">de</span> Acuña. Noticias biográficas Biblioteca\",\"timestamp\":\"2023-12-25T13:42:32Z\"},{\"ns\":0,\"title\":\"Colegio de la Santa Cruz de Tlatelolco\",\"pageid\":697854,\"size\":44343,\"wordcount\":6421,\"snippet\":\"posicionándose en alguno <span class=\\\"searchmatch\\\">de</span> los dos bandos: maximalista y minimalista. En este artículo se ha adoptado las posiciones <span class=\\\"searchmatch\\\">de</span> Rodolfo <span class=\\\"searchmatch\\\">Acuna</span>-Soto, David W. Stahle\",\"timestamp\":\"2024-01-22T18:14:37Z\"},{\"ns\":0,\"title\":\"César Acuña\",\"pageid\":4450099,\"size\":34851,\"wordcount\":3468,\"snippet\":\"and <span class=\\\"searchmatch\\\">Acuna</span> banned from election». BBC News (en inglés británico). 9 <span class=\\\"searchmatch\\\">de</span> marzo <span class=\\\"searchmatch\\\">de</span> 2016. Consultado el 4 <span class=\\\"searchmatch\\\">de</span> diciembre <span class=\\\"searchmatch\\\">de</span> 2020.  EsthefanyMolina (29 <span class=\\\"searchmatch\\\">de</span> enero\",\"timestamp\":\"2024-01-29T03:09:20Z\"},{\"ns\":0,\"title\":\"Antonio de Acuña y Cabrera\",\"pageid\":121971,\"size\":4135,\"wordcount\":439,\"snippet\":\"opiniones se encontraban divididas entre Ambrosio <span class=\\\"searchmatch\\\">de</span> Urra, <span class=\\\"searchmatch\\\">Juan</span> Fernández Rebolledo y Francisco <span class=\\\"searchmatch\\\">de</span> la Fuente Villalobos. Se eligió a este último para\",\"timestamp\":\"2024-01-05T14:56:07Z\"},{\"ns\":0,\"title\":\"La isla: El reality\",\"pageid\":9521898,\"size\":147912,\"wordcount\":1613,\"snippet\":\"motivos <span class=\\\"searchmatch\\\">de</span> patrocinio La Isla Rexona y en su última temporada como La isla: Desafío en Turquía es un Reality Show mexicano producido por <span class=\\\"searchmatch\\\">Acun</span>Medya en\",\"timestamp\":\"2024-01-18T17:54:26Z\"},{\"ns\":0,\"title\":\"Judith Grimes\",\"pageid\":9280258,\"size\":34830,\"wordcount\":3875,\"snippet\":\"Alfredo L. (25 <span class=\\\"searchmatch\\\">de</span> noviembre <span class=\\\"searchmatch\\\">de</span> 2019). «‘The Walking Dead’ 10×08: Nunca olvides tu verdadero hogar». Cienmegas.  <span class=\\\"searchmatch\\\">Acuna</span>, Kirsten (6 <span class=\\\"searchmatch\\\">de</span> noviembre <span class=\\\"searchmatch\\\">de</span> 2019). «How\",\"timestamp\":\"2024-01-09T18:23:54Z\"},{\"ns\":0,\"title\":\"Provincia de Mendoza\",\"pageid\":11253,\"size\":100400,\"wordcount\":11335,\"snippet\":\"Está ubicada al suroeste <span class=\\\"searchmatch\\\">de</span> la región del Nuevo Cuyo y más exactamente el Cuyo, al oeste del país, limitando al norte con San <span class=\\\"searchmatch\\\">Juan</span>, al este con el río Desaguadero\",\"timestamp\":\"2024-01-30T03:18:18Z\"}]}}";
	@Resource
	private FindWikipediaAuthorPort findWikipediaAuthorPort;

	@Test
	public void findWikipediaAuthorOK() {
		// Given
		String subject = "Juan Alonso de Acuna";
		String lang = "es";
		int cont = 5;
		String[] expectedResults = new String[] {
				"Alfonso (o Alonso) Carrillo de Acuña (Carrascosa del Campo, 11 de agosto de 1413-Alcalá de Henares, 1 de julio de 1482). Importante prelado de la Castilla del siglo XV.",
				"https://upload.wikimedia.org/wikipedia/commons/f/f7/Alonso_Carrillo_de_Acu%C3%B1a_1508.gif",
				ProviderEnum.WIKIPEDIA.name() };

		Mockito.when(dataUtils.getData(anyString())).thenReturn(JSON);
		Mockito.doReturn(expectedResults).when(findWikipediaAuthorInfoPort).getAuthorInfo(anyString(), anyString());

		// When
		String[] results = findWikipediaAuthorPort.findAuthor(subject, lang, cont);

		// Then
		assertArrayEquals(expectedResults, results);
	}
}
