package com.github.kaivu.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.github.kaivu.configuration.BaseQuarkusTest;
import com.github.kaivu.utils.TestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests for the entire REST API
 * Tests complete request/response cycles
 */
@QuarkusTest
@DisplayName("End-to-End API Tests")
class ApiEndToEndTest extends BaseQuarkusTest {

    @Test
    @DisplayName("Should handle complete demo workflow")
    void testCompleteDemoWorkflow() {
        // Test the complete workflow from request to response
        var validRequest = TestDataFactory.createValidDemoRequest();
        var requestJson = TestDataFactory.toJson(validRequest);

        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .contentType(APPLICATION_JSON)
                .body(requestJson)
                .when()
                .post("/demo")
                .then()
                .statusCode(200)
                .body("name", equalTo(validRequest.getName()))
                .body("email", equalTo(validRequest.getEmail()))
                .body("status", equalTo(validRequest.getStatus()))
                .body("metadata.key", equalTo(validRequest.getMetadata().getKey()))
                .body("metadata.value", equalTo(validRequest.getMetadata().getValue()));
    }

    @Test
    @DisplayName("Should validate all error scenarios")
    void testAllErrorScenarios() {
        // Test unauthorized access
        given().contentType(APPLICATION_JSON).when().get("/demo/401").then().statusCode(401);

        // Test forbidden access
        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .when()
                .get("/demo/403")
                .then()
                .statusCode(403);

        // Test not found
        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .when()
                .get("/demo/404")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should handle validation errors properly")
    void testValidationErrors() {
        var invalidRequest = TestDataFactory.createInvalidNameDemoRequest();
        var requestJson = TestDataFactory.toJson(invalidRequest);

        given().header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .contentType(APPLICATION_JSON)
                .body(requestJson)
                .when()
                .post("/demo")
                .then()
                .statusCode(400); // Just check for 400 status, don't check response structure
    }
}
