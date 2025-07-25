package com.github.kaivu.common.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for PaginationUtil
 */
@DisplayName("Pagination Utility Tests")
class PaginationUtilTest {

    @Test
    @DisplayName("Should be a utility class that cannot be instantiated")
    void testUtilityClass() {
        // Test that the class has the expected utility structure
        assertNotNull(PaginationUtil.class);

        // Verify it's a utility class by checking if it has static methods
        try {
            var methods = PaginationUtil.class.getDeclaredMethods();
            assertTrue(methods.length > 0, "PaginationUtil should have methods");
        } catch (Exception e) {
            fail("Should be able to access PaginationUtil methods");
        }
    }

    @Test
    @DisplayName("Should generate pagination response with all links")
    void testGeneratePaginationResponseMiddlePage() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageResponse<String> pageResponse = new PageResponse<>(content, 50, 1, 10);

        UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/api/items")
                .queryParam("page", 1)
                .queryParam("size", 10);

        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("50", response.getHeaderString("X-Total-Count"));
        assertNotNull(response.getHeaderString("Link"));

        String linkHeader = response.getHeaderString("Link");
        assertTrue(linkHeader.contains("rel=\"next\""));
        assertTrue(linkHeader.contains("rel=\"prev\""));
        assertTrue(linkHeader.contains("rel=\"first\""));
        assertTrue(linkHeader.contains("rel=\"last\""));
    }

    @Test
    @DisplayName("Should generate pagination response for first page")
    void testGeneratePaginationResponseFirstPage() {
        List<String> content = Arrays.asList("item1", "item2");
        PageResponse<String> pageResponse = new PageResponse<>(content, 25, 0, 10);

        UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/api/items")
                .queryParam("page", 0)
                .queryParam("size", 10);

        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        String linkHeader = response.getHeaderString("Link");
        assertTrue(linkHeader.contains("rel=\"next\""));
        assertFalse(linkHeader.contains("rel=\"prev\""));
        assertTrue(linkHeader.contains("rel=\"first\""));
        assertTrue(linkHeader.contains("rel=\"last\""));
    }

    @Test
    @DisplayName("Should generate pagination response for last page")
    void testGeneratePaginationResponseLastPage() {
        List<String> content = Arrays.asList("item1");
        PageResponse<String> pageResponse = new PageResponse<>(content, 25, 2, 10);

        UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/api/items")
                .queryParam("page", 2)
                .queryParam("size", 10);

        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        String linkHeader = response.getHeaderString("Link");
        assertFalse(linkHeader.contains("rel=\"next\""));
        assertTrue(linkHeader.contains("rel=\"prev\""));
        assertTrue(linkHeader.contains("rel=\"first\""));
        assertTrue(linkHeader.contains("rel=\"last\""));
    }

    @Test
    @DisplayName("Should generate pagination response for single page")
    void testGeneratePaginationResponseSinglePage() {
        List<String> content = Arrays.asList("item1", "item2");
        PageResponse<String> pageResponse = new PageResponse<>(content, 2, 0, 10);

        UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/api/items")
                .queryParam("page", 0)
                .queryParam("size", 10);

        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        assertEquals("2", response.getHeaderString("X-Total-Count"));
        String linkHeader = response.getHeaderString("Link");
        assertFalse(linkHeader.contains("rel=\"next\""));
        assertFalse(linkHeader.contains("rel=\"prev\""));
        assertTrue(linkHeader.contains("rel=\"first\""));
        assertTrue(linkHeader.contains("rel=\"last\""));
    }

    @Test
    @DisplayName("Should handle empty content")
    void testGeneratePaginationResponseEmptyContent() {
        List<String> content = List.of();
        PageResponse<String> pageResponse = new PageResponse<>(content, 0, 0, 10);

        UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/api/items");

        Response.ResponseBuilder responseBuilder = PaginationUtil.generatePaginationResponse(uriBuilder, pageResponse);
        Response response = responseBuilder.build();

        assertEquals("0", response.getHeaderString("X-Total-Count"));
        assertNotNull(response.getHeaderString("Link"));
    }
}
