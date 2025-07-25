package com.github.kaivu.adapter.out.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.common.constant.EntitiesConstant;
import com.github.kaivu.common.constant.ErrorsKeyConstant;
import org.junit.jupiter.api.Test;

class ClientErrorsEnumTest {

    @Test
    void testEnumValues() {
        ClientErrorsEnum[] values = ClientErrorsEnum.values();
        assertEquals(5, values.length);

        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST, values[0]);
        assertEquals(ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR, values[1]);
        assertEquals(ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED, values[2]);
        assertEquals(ClientErrorsEnum.DEMO_REST_UNAUTHORIZED, values[3]);
        assertEquals(ClientErrorsEnum.DEMO_REST_CONFLICT, values[4]);
    }

    @Test
    void testGetEntityName() {
        assertEquals(EntitiesConstant.DEMO_REST, ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST.getEntityName());
        assertEquals(
                EntitiesConstant.DEMO_REST, ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR.getEntityName());
        assertEquals(EntitiesConstant.DEMO_REST, ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED.getEntityName());
        assertEquals(EntitiesConstant.DEMO_REST, ClientErrorsEnum.DEMO_REST_UNAUTHORIZED.getEntityName());
        assertEquals(EntitiesConstant.DEMO_REST, ClientErrorsEnum.DEMO_REST_CONFLICT.getEntityName());
    }

    @Test
    void testGetErrorKey() {
        assertEquals(ErrorsKeyConstant.CLIENT_BAD_REQUEST, ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST.getErrorKey());
        assertEquals(
                ErrorsKeyConstant.INTERNAL_SERVER_ERROR,
                ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR.getErrorKey());
        assertEquals(ErrorsKeyConstant.PERMISSION_DENIED, ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED.getErrorKey());
        assertEquals(ErrorsKeyConstant.UNAUTHORIZED, ClientErrorsEnum.DEMO_REST_UNAUTHORIZED.getErrorKey());
        assertEquals(ErrorsKeyConstant.CONFLICT, ClientErrorsEnum.DEMO_REST_CONFLICT.getErrorKey());
    }

    @Test
    void testGetFullKey() {
        String expectedPrefix = EntitiesConstant.DEMO_REST + ".";

        assertEquals(
                expectedPrefix + ErrorsKeyConstant.CLIENT_BAD_REQUEST,
                ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST.getFullKey());
        assertEquals(
                expectedPrefix + ErrorsKeyConstant.INTERNAL_SERVER_ERROR,
                ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR.getFullKey());
        assertEquals(
                expectedPrefix + ErrorsKeyConstant.PERMISSION_DENIED,
                ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED.getFullKey());
        assertEquals(
                expectedPrefix + ErrorsKeyConstant.UNAUTHORIZED, ClientErrorsEnum.DEMO_REST_UNAUTHORIZED.getFullKey());
        assertEquals(expectedPrefix + ErrorsKeyConstant.CONFLICT, ClientErrorsEnum.DEMO_REST_CONFLICT.getFullKey());
    }

    @Test
    void testGetByName_ValidNames() {
        assertEquals(
                ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST,
                ClientErrorsEnum.getByName("DEMO_REST_CLIENT_BAD_REQUEST"));
        assertEquals(
                ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR,
                ClientErrorsEnum.getByName("DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR"));
        assertEquals(
                ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED,
                ClientErrorsEnum.getByName("DEMO_REST_PERMISSION_DENIED"));
        assertEquals(ClientErrorsEnum.DEMO_REST_UNAUTHORIZED, ClientErrorsEnum.getByName("DEMO_REST_UNAUTHORIZED"));
        assertEquals(ClientErrorsEnum.DEMO_REST_CONFLICT, ClientErrorsEnum.getByName("DEMO_REST_CONFLICT"));
    }

    @Test
    void testGetByName_InvalidName() {
        assertNull(ClientErrorsEnum.getByName("INVALID_NAME"));
        assertNull(ClientErrorsEnum.getByName(""));
        assertNull(ClientErrorsEnum.getByName(null));
    }
}
