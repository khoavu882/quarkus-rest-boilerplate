package com.github.kaivu.adapter.out.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.adapter.out.handler.ClientErrorsEnum;
import org.junit.jupiter.api.Test;

class ClientExceptionTest {

    @Test
    void testConstructorWithEntityNameAndErrorKey() {
        String entityName = "TestEntity";
        String errorKey = "TEST_ERROR";
        Throwable cause = new RuntimeException("Test cause");

        ClientException exception = new ClientException(entityName, errorKey, cause);

        assertEquals(entityName, exception.getEntityName());
        assertEquals(errorKey, exception.getErrorKey());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getErrorsEnum());
        assertFalse(exception.getPassThrough());
        assertNull(exception.getClientMessage());
    }

    @Test
    void testConstructorWithErrorsEnum() {
        ClientErrorsEnum errorsEnum = ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST;
        Throwable cause = new RuntimeException("Test cause");

        ClientException exception = new ClientException(errorsEnum, cause);

        assertEquals(errorsEnum.getEntityName(), exception.getEntityName());
        assertEquals(errorsEnum.getErrorKey(), exception.getErrorKey());
        assertEquals(errorsEnum, exception.getErrorsEnum());
        assertEquals(cause, exception.getCause());
        assertFalse(exception.getPassThrough());
        assertNull(exception.getClientMessage());
    }

    @Test
    void testConstructorWithAllParameters() {
        ClientErrorsEnum errorsEnum = ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR;
        Boolean passThrough = true;
        String clientMessage = "Client error message";
        Throwable cause = new RuntimeException("Test cause");

        ClientException exception = new ClientException(errorsEnum, passThrough, clientMessage, cause);

        assertEquals(errorsEnum.getEntityName(), exception.getEntityName());
        assertEquals(errorsEnum.getErrorKey(), exception.getErrorKey());
        assertEquals(errorsEnum, exception.getErrorsEnum());
        assertEquals(passThrough, exception.getPassThrough());
        assertEquals(clientMessage, exception.getClientMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testToString() {
        ClientErrorsEnum errorsEnum = ClientErrorsEnum.DEMO_REST_UNAUTHORIZED;
        Throwable cause = new RuntimeException("Test cause");

        ClientException exception = new ClientException(errorsEnum, cause);
        String toString = exception.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ClientException"));
    }
}
