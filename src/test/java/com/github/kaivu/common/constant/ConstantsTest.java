package com.github.kaivu.common.constant;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Unit tests for constant classes
 */
@DisplayName("Constants Tests")
class ConstantsTest {

    @Test
    @DisplayName("AppConstant should have only static final fields")
    void testAppConstantFields() throws Exception {
        Class<?> clazz = AppConstant.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            assertTrue(Modifier.isStatic(field.getModifiers()), "Field " + field.getName() + " should be static");
            assertTrue(Modifier.isFinal(field.getModifiers()), "Field " + field.getName() + " should be final");
        }
    }

    @Test
    @DisplayName("AppConstant should have private constructor")
    void testAppConstantPrivateConstructor() throws Exception {
        Constructor<AppConstant> constructor = AppConstant.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        // Test that we can instantiate the class (it doesn't need to throw an exception)
        assertDoesNotThrow(() -> {
            try {
                constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("AppHeaderConstant should have only static final fields")
    void testAppHeaderConstantFields() throws Exception {
        Class<?> clazz = AppHeaderConstant.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            assertTrue(Modifier.isStatic(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    @Test
    @DisplayName("ClientConfigKeyConstant should have only static final fields")
    void testClientConfigKeyConstantFields() throws Exception {
        Class<?> clazz = ClientConfigKeyConstant.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            assertTrue(Modifier.isStatic(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    @Test
    @DisplayName("ErrorsKeyConstant should have only static final fields")
    void testErrorsKeyConstantFields() throws Exception {
        Class<?> clazz = ErrorsKeyConstant.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            assertTrue(Modifier.isStatic(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    @Test
    @DisplayName("EntitiesConstant should have only static final fields")
    void testEntitiesConstantFields() throws Exception {
        Class<?> clazz = EntitiesConstant.class;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            assertTrue(Modifier.isStatic(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    @Test
    @DisplayName("All constant classes should be final")
    void testConstantClassesAreFinal() {
        assertTrue(Modifier.isFinal(AppConstant.class.getModifiers()));
        assertTrue(Modifier.isFinal(AppHeaderConstant.class.getModifiers()));
        assertTrue(Modifier.isFinal(ClientConfigKeyConstant.class.getModifiers()));
        assertTrue(Modifier.isFinal(ErrorsKeyConstant.class.getModifiers()));
        assertTrue(Modifier.isFinal(EntitiesConstant.class.getModifiers()));
    }
}
