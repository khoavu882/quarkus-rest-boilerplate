package com.github.kaivu.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kaivu.adapter.in.rest.DemoResource;
import com.github.kaivu.domain.enumeration.ActionStatus;

/**
 * Test utilities for creating test data and common test operations
 */
public class TestDataFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a valid DemoRequest for testing
     */
    public static DemoResource.DemoRequest createValidDemoRequest() {
        DemoResource.DemoRequest request = new DemoResource.DemoRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setStatus(ActionStatus.ACTIVATED.name());

        DemoResource.DemoRequest.Metadata metadata = new DemoResource.DemoRequest.Metadata();
        metadata.setKey("testKey");
        metadata.setValue("testValue");
        request.setMetadata(metadata);

        return request;
    }

    /**
     * Creates a DemoRequest with invalid name for testing validation
     */
    public static DemoResource.DemoRequest createInvalidNameDemoRequest() {
        DemoResource.DemoRequest request = createValidDemoRequest();
        request.setName("ab"); // Too short
        return request;
    }

    /**
     * Creates a DemoRequest with invalid email for testing validation
     */
    public static DemoResource.DemoRequest createInvalidEmailDemoRequest() {
        DemoResource.DemoRequest request = createValidDemoRequest();
        request.setEmail("invalid-email");
        return request;
    }

    /**
     * Converts object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}
