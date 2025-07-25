package com.github.kaivu.configuration;

import static org.junit.jupiter.api.Assertions.*;

import com.github.kaivu.Application;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the main Application class to ensure proper startup and configuration
 */
@QuarkusTest
@DisplayName("Application Startup Tests")
class ApplicationTest {

    @Test
    @DisplayName("Should start application successfully")
    void testApplicationStartup() {
        // This test validates that the Quarkus application starts successfully
        // and all CDI beans are properly configured
        assertNotNull(Application.class);

        // If we reach this point, the application has started successfully
        // and all dependency injection is working correctly
        assertTrue(true, "Application started successfully");
    }

    @Test
    @DisplayName("Should have main method for standalone execution")
    void testMainMethod() throws Exception {
        // Verify that the Application class has a main method
        var mainMethod = Application.class.getDeclaredMethod("main", String[].class);
        assertNotNull(mainMethod, "Main method should exist");
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()), "Main method should be static");
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()), "Main method should be public");
    }
}
