package com.github.kaivu.configuration.handler.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.configuration.handler.ErrorResponse;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for EntityNotFoundExceptionMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EntityNotFoundExceptionMapper Unit Tests")
class EntityNotFoundExceptionMapperTest {

    @Mock
    private ContainerRequestContext requestContext;

    @InjectMocks
    private EntityNotFoundExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn("test-trace-id-123");
    }

    @Test
    @DisplayName("Should map EntityNotFoundException to 404 response")
    void testToResponse_Success() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException(ErrorsEnum.ENTITY_DEVICE_NOT_FOUND);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals("test-trace-id-123", errorResponse.getErrorId());
        assertNotNull(errorResponse.getErrors());
        assertEquals(1, errorResponse.getErrors().size());

        var errorMessage = errorResponse.getErrors().get(0);
        assertNotNull(errorMessage.getErrorKey());
        assertNotNull(errorMessage.getMessage());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }

    @Test
    @DisplayName("Should handle exception with null trace ID")
    void testToResponse_NullTraceId() {
        // Given
        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(null);
        EntityNotFoundException exception = new EntityNotFoundException(ErrorsEnum.ENTITY_DEVICE_NOT_FOUND);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertNull(errorResponse.getErrorId());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }

    @Test
    @DisplayName("Should handle user not found exception")
    void testToResponse_UserNotFound() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException(ErrorsEnum.USER_NOT_FOUND);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(1, errorResponse.getErrors().size());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }
}
