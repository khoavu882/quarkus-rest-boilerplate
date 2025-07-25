package com.github.kaivu.common.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for PaginationUtil
 */
@DisplayName("PaginationUtil Unit Tests")
class PaginationUtilTest {

    private UriBuilder uriBuilder;
    private PageResponse<String> pageResponse;

    @BeforeEach
    void setUp() {
        uriBuilder = mock(UriBuilder.class);
        UriBuilder clonedBuilder = mock(UriBuilder.class);

        // Mock the full UriBuilder chain properly
        when(uriBuilder.clone()).thenReturn(clonedBuilder);
        when(clonedBuilder.resolveTemplate(anyString(), any())).thenReturn(clonedBuilder);
        when(clonedBuilder.replaceQueryParam(anyString(), any())).thenReturn(clonedBuilder);
        when(clonedBuilder.build()).thenReturn(URI.create("http://example.com/api/test"));
        when(clonedBuilder.toTemplate()).thenReturn("http://example.com/api/test?page={page}&size={size}");

        // Mock the original builder for direct calls
        when(uriBuilder.resolveTemplate(anyString(), any())).thenReturn(uriBuilder);
        when(uriBuilder.replaceQueryParam(anyString(), any())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://example.com/api/test"));
        when(uriBuilder.toTemplate()).thenReturn("http://example.com/api/test?page={page}&size={size}");
    }

    @Test
    @DisplayName("Should throw exception when trying to instantiate utility class")
    void testConstructorThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            try {
                var constructor = PaginationUtil.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (Exception e) {
                if (e.getCause() instanceof IllegalStateException) {
                    throw (IllegalStateException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Should generate pagination response for first page")
    void testGeneratePaginationResponse_FirstPage() {
        // Given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(0)
                .size(10)
                .totalElements(25)
                .build();

        // When
        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        // Then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(content, response.getEntity());
        assertEquals("25", response.getHeaderString("X-Total-Count"));
        assertNotNull(response.getHeaderString("Link"));
    }

    @Test
    @DisplayName("Should generate pagination response for middle page")
    void testGeneratePaginationResponse_MiddlePage() {
        // Given
        List<String> content = Arrays.asList("item4", "item5", "item6");
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(1)
                .size(3)
                .totalElements(9)
                .build();

        // When
        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        // Then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(content, response.getEntity());
        assertEquals("9", response.getHeaderString("X-Total-Count"));

        String linkHeader = response.getHeaderString("Link");
        assertNotNull(linkHeader);
        assertTrue(linkHeader.contains("next"));
        assertTrue(linkHeader.contains("prev"));
        assertTrue(linkHeader.contains("first"));
        assertTrue(linkHeader.contains("last"));
    }

    @Test
    @DisplayName("Should generate pagination response for last page")
    void testGeneratePaginationResponse_LastPage() {
        // Given
        List<String> content = Arrays.asList("item7", "item8", "item9");
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(2)
                .size(3)
                .totalElements(9)
                .build();

        // When
        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        // Then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(content, response.getEntity());
        assertEquals("9", response.getHeaderString("X-Total-Count"));

        String linkHeader = response.getHeaderString("Link");
        assertNotNull(linkHeader);
        assertFalse(linkHeader.contains("next"));
        assertTrue(linkHeader.contains("prev"));
        assertTrue(linkHeader.contains("first"));
        assertTrue(linkHeader.contains("last"));
    }

    @Test
    @DisplayName("Should generate pagination response for single page")
    void testGeneratePaginationResponse_SinglePage() {
        // Given
        List<String> content = Arrays.asList("item1", "item2");
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(0)
                .size(10)
                .totalElements(2)
                .build();

        // When
        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        // Then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(content, response.getEntity());
        assertEquals("2", response.getHeaderString("X-Total-Count"));

        String linkHeader = response.getHeaderString("Link");
        assertNotNull(linkHeader);
        assertFalse(linkHeader.contains("next"));
        assertFalse(linkHeader.contains("prev"));
        assertTrue(linkHeader.contains("first"));
        assertTrue(linkHeader.contains("last"));
    }

    @Test
    @DisplayName("Should generate pagination response for empty page")
    void testGeneratePaginationResponse_EmptyPage() {
        // Given
        List<String> content = Collections.emptyList();
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(0)
                .size(10)
                .totalElements(0)
                .build();

        // When
        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        // Then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(content, response.getEntity());
        assertEquals("0", response.getHeaderString("X-Total-Count"));
        assertNotNull(response.getHeaderString("Link"));
    }

    @Test
    @DisplayName("Should handle large page numbers correctly")
    void testGeneratePaginationResponse_LargePageNumbers() {
        // Given
        List<String> content = Arrays.asList("item1000");
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(99)
                .size(1)
                .totalElements(100)
                .build();

        // When
        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        // Then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("100", response.getHeaderString("X-Total-Count"));

        String linkHeader = response.getHeaderString("Link");
        assertNotNull(linkHeader);
        assertTrue(linkHeader.contains("prev"));
        assertTrue(linkHeader.contains("first"));
    }

    @Test
    @DisplayName("Should verify UriBuilder interactions")
    void testUriBuilderInteractions() {
        // Given
        List<String> content = Arrays.asList("item1");
        pageResponse = PageResponse.<String>builder()
                .content(content)
                .page(1)
                .size(5)
                .totalElements(15)
                .build();

        // When
        PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);

        // Then
        verify(uriBuilder, atLeastOnce()).clone();
        verify(uriBuilder, atLeastOnce()).replaceQueryParam(eq("page"), any());
        verify(uriBuilder, atLeastOnce()).replaceQueryParam(eq("size"), any());
        verify(uriBuilder, atLeastOnce()).build();
    }
}
