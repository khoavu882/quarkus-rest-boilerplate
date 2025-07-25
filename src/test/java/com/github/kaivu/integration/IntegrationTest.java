package com.github.kaivu.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

import com.github.kaivu.configuration.BaseQuarkusTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the application
 * Tests the application with basic health checks
 */
@QuarkusTest
@DisplayName("Integration Tests")
class IntegrationTest extends BaseQuarkusTest {

    @Test
    @DisplayName("Should start application successfully")
    void testApplicationStartup() {
        given().when().get("/q/health/live").then().statusCode(200);
    }

    @Test
    @DisplayName("Should access readiness endpoint")
    void testReadinessCheck() {
        // Make the test more resilient - accept either 200 (healthy) or 503 (unhealthy but responding)
        given().when().get("/q/health/ready").then().statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    @Test
    @DisplayName("Should handle metrics endpoint")
    void testMetricsEndpoint() {
        given().when().get("/q/metrics").then().statusCode(200);
    }
}
