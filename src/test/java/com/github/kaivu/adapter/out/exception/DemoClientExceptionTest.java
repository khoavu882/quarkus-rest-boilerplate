package com.github.kaivu.adapter.out.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.github.kaivu.adapter.out.handler.ClientErrorsEnum;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class DemoClientExceptionTest {

    @Test
    void testConstructorWithEntityNameErrorKeyAndResponse() {
        String entityName = "TestEntity";
        String errorKey = "TEST_ERROR";
        Throwable cause = new RuntimeException("Test cause");
        Response response = mock(Response.class);

        DemoClientException exception = new DemoClientException(entityName, errorKey, cause, response);

        assertEquals(entityName, exception.getEntityName());
        assertEquals(errorKey, exception.getErrorKey());
        assertEquals(cause, exception.getCause());
        assertEquals(response, exception.getResponse());
    }

    @Test
    void testConstructorWithErrorsEnumCauseAndResponse() {
        ClientErrorsEnum errorsEnum = ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST;
        Throwable cause = new RuntimeException("Test cause");
        Response response = mock(Response.class);

        DemoClientException exception = new DemoClientException(errorsEnum, cause, response);

        assertEquals(errorsEnum.getEntityName(), exception.getEntityName());
        assertEquals(errorsEnum.getErrorKey(), exception.getErrorKey());
        assertEquals(errorsEnum, exception.getErrorsEnum());
        assertEquals(cause, exception.getCause());
        assertEquals(response, exception.getResponse());
    }

    @Test
    void testConstructorWithErrorsEnumAndResponse() {
        ClientErrorsEnum errorsEnum = ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED;
        Response response = mock(Response.class);

        DemoClientException exception = new DemoClientException(errorsEnum, response);

        assertEquals(errorsEnum.getEntityName(), exception.getEntityName());
        assertEquals(errorsEnum.getErrorKey(), exception.getErrorKey());
        assertEquals(errorsEnum, exception.getErrorsEnum());
        assertNull(exception.getCause());
        assertEquals(response, exception.getResponse());
    }

    @Test
    void testConstructorWithResponseOnly() {
        Response response = mock(Response.class);

        DemoClientException exception = new DemoClientException(response);

        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST.getEntityName(), exception.getEntityName());
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST.getErrorKey(), exception.getErrorKey());
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST, exception.getErrorsEnum());
        assertNull(exception.getCause());
        assertEquals(response, exception.getResponse());
    }

    @Test
    void testToString() {
        Response response = mock(Response.class);
        DemoClientException exception = new DemoClientException(response);
        String toString = exception.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("DemoClientException"));
    }
}
