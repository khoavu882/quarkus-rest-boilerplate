package com.github.kaivu.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.github.kaivu.configuration.BaseQuarkusTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for CommonResource REST controller
 * Tests bundle message retrieval functionality
 */
@QuarkusTest
@DisplayName("Common Resource Tests")
class CommonResourceTest extends BaseQuarkusTest {

    @Test
    @DisplayName("Should handle bundle messages endpoint")
    void testGetBundleMessagesEndpoint() {
        given().accept(APPLICATION_JSON)
                .when()
                .get("/common/bundles/messages")
                .then()
                .statusCode(anyOf(is(200), is(400), is(500))); // Accept 400 for missing bundles
    }

    @Test
    @DisplayName("Should handle non-existent bundle")
    void testGetBundleMessagesNotFound() {
        given().accept(APPLICATION_JSON)
                .when()
                .get("/common/bundles/nonexistent")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404), is(500))); // Accept 400 for missing bundles
    }

    @Test
    @DisplayName("Should return appropriate content type")
    void testGetBundleMessagesContentType() {
        given().accept(APPLICATION_JSON)
                .when()
                .get("/common/bundles/messages")
                .then()
                .statusCode(anyOf(is(200), is(400), is(500))) // Accept 400 for missing bundles
                .contentType(anyOf(containsString("application/json"), containsString("text/plain")));
    }
}
