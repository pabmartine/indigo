package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.common.model.Serie;
import com.martinia.indigo.serie.domain.ports.repositories.SerieRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindCoverSerieUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private SerieRepository serieRepository;

	@Resource
	private FindCoverSerieUseCase findCoverSerieUseCase;


	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetCover_WithValidSerie_ShouldReturnCover() throws Exception {
		// Given
		String serie = "TestSerie";
		List<Book> books = new ArrayList<>();
		books.add(createBook("Book1", "Cover1", 1));
		books.add(createBook("Book2", "Cover2", 2));
		when(serieRepository.findBooksBySerie(serie.replace("@_@", "&"))).thenReturn(books);

		// When
		String cover = findCoverSerieUseCase.getCover(serie);

		// Then
		assertEquals("Cover1", cover);
	}

	@Test
	public void testGetCover_WithEmptySerie_ShouldReturnNull() throws Exception {
		// Given
		String serie = "";
		when(serieRepository.findBooksBySerie(serie.replace("@_@", "&"))).thenReturn(new ArrayList<>());

		// When
		String cover = findCoverSerieUseCase.getCover(serie);

		// Then
		assertEquals(null, cover);
	}

	private Book createBook(String id, String image, int index) {
		Book book = new Book();
		book.setId(id);
		book.setImage(image);
		Serie serie = new Serie();
		serie.setIndex(index);
		book.setSerie(serie);
		return book;
	}

	@Test
	public void testGetCover_WithNoBooks_ShouldReturnNull() throws Exception {
		// Given
		String serie = "TestSerie";
		when(serieRepository.findBooksBySerie(serie.replace("@_@", "&"))).thenReturn(new ArrayList<>());

		// When
		String cover = findCoverSerieUseCase.getCover(serie);

		// Then
		assertEquals(null, cover);
	}

	@Test
	public void testGetCover_WithMultipleBooks_ShouldReturnFirstCover() throws Exception {
		// Given
		String serie = "TestSerie";
		List<Book> books = new ArrayList<>();
		books.add(createBook("Book1", "Cover1", 3));
		books.add(createBook("Book2", "Cover2", 2));
		books.add(createBook("Book3", "Cover3", 1));
		when(serieRepository.findBooksBySerie(serie.replace("@_@", "&"))).thenReturn(books);

		// When
		String cover = findCoverSerieUseCase.getCover(serie);

		// Then
		assertEquals("Cover3", cover);
	}

}
