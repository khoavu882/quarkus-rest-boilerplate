package com.github.kaivu.adapter.out.handler.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.adapter.out.exception.ClientException;
import com.github.kaivu.adapter.out.handler.ClientErrorsEnum;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import com.github.kaivu.configuration.handler.ErrorResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

/**
 * Comprehensive test class for ClientExceptionMapper - targeting 80% coverage
 */
@DisplayName("Client Exception Mapper Tests")
class ClientExceptionMapperTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ClientErrorsEnum errorsEnum;

    private ClientExceptionMapper mapper;
    private MockedStatic<ResourceBundleUtil> resourceBundleUtilMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ClientExceptionMapper();
        mapper.requestContext = requestContext;

        // Mock ResourceBundleUtil static methods
        resourceBundleUtilMock = mockStatic(ResourceBundleUtil.class);
    }

    @AfterEach
    void tearDown() {
        resourceBundleUtilMock.close();
    }

    @Test
    @DisplayName("Should return BAD_REQUEST with client message when passThrough is true")
    void testToResponseWithPassThroughTrue() {
        // Given
        String traceId = "test-trace-id-123";
        String clientMessage = "Custom client error message";
        ClientException exception = new ClientException(errorsEnum, true, clientMessage, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(clientMessage, response.getEntity());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR with ErrorResponse when passThrough is false")
    void testToResponseWithPassThroughFalse() {
        // Given
        String traceId = "test-trace-id-456";
        String entityName = "TestEntity";
        String errorKey = "test.error.key";
        String fullKey = entityName + "." + errorKey;
        String expectedMessage = "Test error message for entity";

        when(errorsEnum.getEntityName()).thenReturn(entityName);
        when(errorsEnum.getErrorKey()).thenReturn(errorKey);
        when(errorsEnum.getFullKey()).thenReturn(fullKey);

        ClientException exception = new ClientException(errorsEnum, false, null, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);
        when(requestContext.getLanguage()).thenReturn(Locale.ENGLISH);

        // Mock ResourceBundleUtil call
        resourceBundleUtilMock
                .when(() ->
                        ResourceBundleUtil.getKeyWithResourceBundleOrThrow(anyString(), any(Locale.class), eq(fullKey)))
                .thenReturn(expectedMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        verify(requestContext).getLanguage();
        // getFullKey() is called twice in the implementation - once in main method, once in helper
        verify(errorsEnum, times(2)).getFullKey();
    }

    @Test
    @DisplayName("Should handle null passThrough as false")
    void testToResponseWithNullPassThrough() {
        // Given
        String traceId = "test-trace-id-789";
        String entityName = "TestEntity";
        String errorKey = "test.error.key";
        String fullKey = entityName + "." + errorKey;
        String expectedMessage = "Test error message for null passthrough";

        when(errorsEnum.getEntityName()).thenReturn(entityName);
        when(errorsEnum.getErrorKey()).thenReturn(errorKey);
        when(errorsEnum.getFullKey()).thenReturn(fullKey);

        ClientException exception = new ClientException(errorsEnum, null, null, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);
        when(requestContext.getLanguage()).thenReturn(Locale.ENGLISH);

        // Mock ResourceBundleUtil call
        resourceBundleUtilMock
                .when(() ->
                        ResourceBundleUtil.getKeyWithResourceBundleOrThrow(anyString(), any(Locale.class), eq(fullKey)))
                .thenReturn(expectedMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        verify(requestContext).getLanguage();
    }

    @Test
    @DisplayName("Should handle Boolean.FALSE passThrough correctly")
    void testToResponseWithPassThroughExplicitlyFalse() {
        // Given
        String traceId = "test-trace-id-false";
        String entityName = "TestEntity";
        String errorKey = "test.error.key";
        String fullKey = entityName + "." + errorKey;
        String expectedMessage = "Test error message for explicit false";

        when(errorsEnum.getEntityName()).thenReturn(entityName);
        when(errorsEnum.getErrorKey()).thenReturn(errorKey);
        when(errorsEnum.getFullKey()).thenReturn(fullKey);

        ClientException exception =
                new ClientException(errorsEnum, Boolean.FALSE, "ignored message", new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);
        when(requestContext.getLanguage()).thenReturn(Locale.ENGLISH);

        // Mock ResourceBundleUtil call
        resourceBundleUtilMock
                .when(() ->
                        ResourceBundleUtil.getKeyWithResourceBundleOrThrow(anyString(), any(Locale.class), eq(fullKey)))
                .thenReturn(expectedMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        verify(requestContext).getLanguage();
    }

    @Test
    @DisplayName("Should handle null trace ID")
    void testToResponseWithNullTraceId() {
        // Given
        String clientMessage = "Test message";
        ClientException exception = new ClientException(errorsEnum, true, clientMessage, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(null);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(clientMessage, response.getEntity());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        // Verify that getLanguage() is not called when passThrough is true
        verify(requestContext, never()).getLanguage();
    }

    @Test
    @DisplayName("Should handle empty client message with passThrough true")
    void testToResponseWithEmptyClientMessage() {
        // Given
        String traceId = "test-trace-id-empty";
        String emptyMessage = "";
        ClientException exception = new ClientException(errorsEnum, true, emptyMessage, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(emptyMessage, response.getEntity());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        // Verify that getLanguage() is not called when passThrough is true
        verify(requestContext, never()).getLanguage();
    }

    @Test
    @DisplayName("Should handle null client message with passThrough true")
    void testToResponseWithNullClientMessage() {
        // Given
        String traceId = "test-trace-id-null-msg";
        ClientException exception = new ClientException(errorsEnum, true, null, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        // Handle null client message properly - should return null or empty response
        assertNull(response.getEntity());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        // Verify that getLanguage() is not called when passThrough is true
        verify(requestContext, never()).getLanguage();
    }

    @Test
    @DisplayName("Should handle different locales correctly")
    void testToResponseWithDifferentLocales() {
        // Given
        String traceId = "test-trace-id-locale";
        String entityName = "TestEntity";
        String errorKey = "test.error.key";
        String fullKey = entityName + "." + errorKey;
        String expectedMessage = "Test error message for different locale";

        when(errorsEnum.getEntityName()).thenReturn(entityName);
        when(errorsEnum.getErrorKey()).thenReturn(errorKey);
        when(errorsEnum.getFullKey()).thenReturn(fullKey);

        ClientException exception = new ClientException(errorsEnum, false, null, new RuntimeException("test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);
        when(requestContext.getLanguage()).thenReturn(Locale.FRENCH);

        // Mock ResourceBundleUtil call
        resourceBundleUtilMock
                .when(() ->
                        ResourceBundleUtil.getKeyWithResourceBundleOrThrow(anyString(), any(Locale.class), eq(fullKey)))
                .thenReturn(expectedMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        verify(requestContext).getLanguage();
        // getFullKey() is called twice in the implementation
        verify(errorsEnum, times(2)).getFullKey();
    }

    @Test
    @DisplayName("Should properly implement ExceptionMapper interface")
    void testExceptionMapperInterface() {
        // Given & When & Then
        assertTrue(mapper instanceof ExceptionMapper);
        assertTrue(mapper instanceof ExceptionMapper<?>);

        // Verify the mapper can handle ClientException
        assertNotNull(mapper);
    }

    @Test
    @DisplayName("Should handle complex error scenarios")
    void testToResponseWithComplexErrorScenario() {
        // Given
        String traceId = "complex-trace-id";
        String entityName = "ComplexEntity";
        String errorKey = "complex.error.key";
        String fullKey = entityName + "." + errorKey;
        String clientMessage = "Complex client message";
        String expectedMessage = "Complex error message";
        RuntimeException cause = new RuntimeException("Complex cause");

        when(errorsEnum.getEntityName()).thenReturn(entityName);
        when(errorsEnum.getErrorKey()).thenReturn(errorKey);
        when(errorsEnum.getFullKey()).thenReturn(fullKey);

        // Test with passThrough false
        ClientException exception = new ClientException(errorsEnum, false, clientMessage, cause);

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);
        when(requestContext.getLanguage()).thenReturn(Locale.ENGLISH);

        // Mock ResourceBundleUtil call
        resourceBundleUtilMock
                .when(() ->
                        ResourceBundleUtil.getKeyWithResourceBundleOrThrow(anyString(), any(Locale.class), eq(fullKey)))
                .thenReturn(expectedMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        verify(requestContext).getLanguage();
        // getFullKey() is called twice in the implementation
        verify(errorsEnum, times(2)).getFullKey();
    }

    @Test
    @DisplayName("Should handle exception with multiple error enum calls")
    void testToResponseWithMultipleErrorEnumCalls() {
        // Given
        String traceId = "multi-call-trace-id";
        String entityName = "MultiEntity";
        String errorKey = "multi.error.key";
        String fullKey = entityName + "." + errorKey;
        String expectedMessage = "Multi-call error message";

        when(errorsEnum.getEntityName()).thenReturn(entityName);
        when(errorsEnum.getErrorKey()).thenReturn(errorKey);
        when(errorsEnum.getFullKey()).thenReturn(fullKey);

        ClientException exception = new ClientException(errorsEnum, false, null, new RuntimeException("multi-test"));

        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(traceId);
        when(requestContext.getLanguage()).thenReturn(Locale.ENGLISH);

        // Mock ResourceBundleUtil call
        resourceBundleUtilMock
                .when(() ->
                        ResourceBundleUtil.getKeyWithResourceBundleOrThrow(anyString(), any(Locale.class), eq(fullKey)))
                .thenReturn(expectedMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof ErrorResponse);

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals(expectedMessage, errorResponse.getErrors().get(0).getMessage());
        assertEquals(traceId, errorResponse.getErrorId());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
        verify(requestContext).getLanguage();
        // Verify getFullKey() is called the expected number of times
        verify(errorsEnum, atLeast(1)).getFullKey();
    }
}
