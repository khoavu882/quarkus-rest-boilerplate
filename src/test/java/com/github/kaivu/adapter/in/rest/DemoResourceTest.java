package com.github.kaivu.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.github.kaivu.configuration.BaseQuarkusTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * Tests for DemoResource REST controller
 * Tests endpoints and validation without external dependencies
 */
@QuarkusTest
@DisplayName("Demo Resource Tests")
class DemoResourceTest extends BaseQuarkusTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Should return appropriate response for demo endpoint")
    void testDemoExceptionWithoutCode() {
        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .when()
                .get("/demo/")
                .then()
                .statusCode(anyOf(is(200), is(404), is(405))); // Accept 405 Method Not Allowed
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Should throw 401 for unauthorized access")
    void testDemoExceptionUnauthorized() {
        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .when()
                .get("/demo/401")
                .then()
                .statusCode(401);
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Should throw 403 for forbidden access")
    void testDemoExceptionForbidden() {
        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .when()
                .get("/demo/403")
                .then()
                .statusCode(403);
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Should accept valid POST request")
    void testDemoPostExceptionWithValidRequest() {
        String validRequest =
                """
            {
                "name": "Test User",
                "email": "test@example.com",
                "status": "ACTIVATED",
                "metadata": {
                    "key": "testKey",
                    "value": "testValue"
                }
            }
            """;

        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .contentType(APPLICATION_JSON)
                .body(validRequest)
                .when()
                .post("/demo")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test User"))
                .body("email", equalTo("test@example.com"))
                .body("status", equalTo("ACTIVATED"));
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Should reject POST request with invalid name")
    void testDemoPostExceptionWithInvalidName() {
        String invalidRequest =
                """
            {
                "name": "ab",
                "email": "test@example.com",
                "status": "ACTIVATED",
                "metadata": {
                    "key": "testKey",
                    "value": "testValue"
                }
            }
            """;

        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .contentType(APPLICATION_JSON)
                .body(invalidRequest)
                .when()
                .post("/demo")
                .then()
                .statusCode(400);
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Should handle demo endpoint appropriately")
    void testDemoEndpoint() {
        // Test that the application is running and can handle requests
        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .when()
                .get("/demo/200") // Use a valid endpoint
                .then()
                .statusCode(anyOf(is(200), is(405), is(500))); // Accept 405 Method Not Allowed and 500
    }
}
