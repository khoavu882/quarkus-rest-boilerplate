package com.github.kaivu.configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

/**
 * Tests for LogExecutionTimeInterceptor to ensure proper logging and performance monitoring
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Log Execution Time Interceptor Tests")
class LogExecutionTimeInterceptorTest {

    @Mock
    private InvocationContext invocationContext;

    @Mock
    private Method method;

    @InjectMocks
    private LogExecutionTimeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        when(invocationContext.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("testMethod");
        // Fix the generic type issue
        when(method.getDeclaringClass()).thenReturn((Class) LogExecutionTimeInterceptorTest.class);
    }

    @Test
    @DisplayName("Should intercept method execution and log timing")
    void testInterceptMethodExecution() throws Exception {
        // Given
        String expectedResult = "test result";
        when(invocationContext.proceed()).thenReturn(expectedResult);

        // When
        Object result = interceptor.logExecutionTime(invocationContext);

        // Then
        assertEquals(expectedResult, result);
        verify(invocationContext).proceed();
        verify(invocationContext, atLeastOnce()).getMethod();
    }

    @Test
    @DisplayName("Should handle method execution exceptions")
    void testInterceptMethodExecutionWithException() throws Exception {
        // Given
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(invocationContext.proceed()).thenThrow(expectedException);

        // When & Then
        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> interceptor.logExecutionTime(invocationContext));

        assertEquals("Test exception", thrown.getMessage());
        verify(invocationContext).proceed();
    }

    @Test
    @DisplayName("Should handle null method gracefully")
    void testInterceptWithNullMethod() throws Exception {
        // Given
        when(invocationContext.getMethod()).thenReturn(null);
        String expectedResult = "test result";
        when(invocationContext.proceed()).thenReturn(expectedResult);

        // When
        Object result = interceptor.logExecutionTime(invocationContext);

        // Then
        assertEquals(expectedResult, result);
        verify(invocationContext).proceed();
    }
}
