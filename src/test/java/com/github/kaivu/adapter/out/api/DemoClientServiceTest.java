package com.github.kaivu.adapter.out.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.kaivu.adapter.out.exception.DemoClientException;
import com.github.kaivu.adapter.out.handler.ClientErrorsEnum;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class DemoClientServiceTest {

    @Test
    void testToException_Unauthorized() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(401);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_UNAUTHORIZED, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_Forbidden() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(403);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_Conflict() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(409);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_CONFLICT, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_BadRequest() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_ClientError_450() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(450);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_InternalServerError() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_ServerError_502() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(502);

        RuntimeException exception = DemoClientService.toException(response);

        assertInstanceOf(DemoClientException.class, exception);
        DemoClientException demoException = (DemoClientException) exception;
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR, demoException.getErrorsEnum());
        assertEquals(response, demoException.getResponse());
    }

    @Test
    void testToException_OtherStatus() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);

        RuntimeException exception = DemoClientService.toException(response);

        assertNull(exception);
    }

    @Test
    void testToException_Status300() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(300);

        RuntimeException exception = DemoClientService.toException(response);

        assertNull(exception);
    }

    @Test
    void testToException_Status600() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(600);

        RuntimeException exception = DemoClientService.toException(response);

        assertNull(exception);
    }
}
