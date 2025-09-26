package com.martinia.indigo.book.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.CountAllBooksUseCase;
import com.martinia.indigo.common.domain.model.Search;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CountAllBooksControllerTest extends BaseIndigoTest {

    @Resource
    private MockMvc mockMvc;

    @MockBean
    private CountAllBooksUseCase countAllBooksUseCase;

    @Test
    @WithMockUser
    public void testCountBooks_WithValidSearch_ReturnsCount() throws Exception {
        // Given
        Long expectedCount = 42L;
        when(countAllBooksUseCase.count(any(Search.class))).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(post("/api/book/count/search/advance")
                .contentType("application/json")
                .content("{\"title\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @WithMockUser
    public void testCountBooks_WithEmptySearch_ReturnsCount() throws Exception {
        // Given
        Long expectedCount = 100L;
        when(countAllBooksUseCase.count(any(Search.class))).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(post("/api/book/count/search/advance")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @WithMockUser
    public void testCountBooks_WithNoResults_ReturnsZero() throws Exception {
        // Given
        Long expectedCount = 0L;
        when(countAllBooksUseCase.count(any(Search.class))).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(post("/api/book/count/search/advance")
                .contentType("application/json")
                .content("{\"title\":\"nonexistent\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @WithMockUser
    public void testCountBooks_WithSearchByAuthor_ReturnsCount() throws Exception {
        // Given
        Long expectedCount = 15L;
        when(countAllBooksUseCase.count(any(Search.class))).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(post("/api/book/count/search/advance")
                .contentType("application/json")
                .content("{\"author\":\"J.K. Rowling\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));
    }
}