package com.github.kaivu.configuration.handler.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.configuration.handler.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

/**
 * Unit tests for ConstraintViolationExceptionMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConstraintViolationExceptionMapper Unit Tests")
class ConstraintViolationExceptionMapperTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ConstraintViolation<?> constraintViolation;

    @Mock
    private Path propertyPath;

    @InjectMocks
    private ConstraintViolationExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn("test-trace-id-456");
    }

    @Test
    @DisplayName("Should map ConstraintViolationException to 400 response")
    void testToResponse_Success() {
        // Given
        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("createEntity.name");
        when(constraintViolation.getMessageTemplate()).thenReturn("{validation.name.required}");
        when(constraintViolation.getMessage()).thenReturn("Name is required");

        Set<ConstraintViolation<?>> violations = Set.of(constraintViolation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals("test-trace-id-456", errorResponse.getErrorId());
        assertEquals(1, errorResponse.getErrors().size());

        var errorMessage = errorResponse.getErrors().get(0);
        assertEquals("name", errorMessage.getPath());
        assertEquals("{validation.name.required}", errorMessage.getErrorKey());
        assertEquals("Name is required", errorMessage.getMessage());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }

    @Test
    @DisplayName("Should handle multiple constraint violations")
    void testToResponse_MultipleViolations() {
        // Given
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);

        when(violation1.getPropertyPath()).thenReturn(path1);
        when(path1.toString()).thenReturn("createEntity.name");
        when(violation1.getMessageTemplate()).thenReturn("{validation.name.required}");
        when(violation1.getMessage()).thenReturn("Name is required");

        when(violation2.getPropertyPath()).thenReturn(path2);
        when(path2.toString()).thenReturn("createEntity.description");
        when(violation2.getMessageTemplate()).thenReturn("{validation.description.length}");
        when(violation2.getMessage()).thenReturn("Description too long");

        Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertEquals(2, errorResponse.getErrors().size());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }

    @Test
    @DisplayName("Should handle null trace ID")
    void testToResponse_NullTraceId() {
        // Given
        when(requestContext.getHeaderString(AppHeaderConstant.TRACE_ID)).thenReturn(null);

        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("createEntity.name");
        when(constraintViolation.getMessageTemplate()).thenReturn("{validation.name.required}");
        when(constraintViolation.getMessage()).thenReturn("Name is required");

        Set<ConstraintViolation<?>> violations = Set.of(constraintViolation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertNull(errorResponse.getErrorId());

        verify(requestContext).getHeaderString(AppHeaderConstant.TRACE_ID);
    }
}
