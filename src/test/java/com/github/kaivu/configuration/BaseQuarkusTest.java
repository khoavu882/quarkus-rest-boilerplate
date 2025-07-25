package com.github.kaivu.configuration;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Base test configuration class for Quarkus tests
 * Provides common test setup and utilities
 */
@QuarkusTest
public abstract class BaseQuarkusTest {

    protected static final String APPLICATION_JSON = "application/json";
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String BEARER_TOKEN = "Bearer test-token";

    @Test
    void testApplicationStartup() {
        given().when().get("/q/health/ready").then().statusCode(200);
    }
}
