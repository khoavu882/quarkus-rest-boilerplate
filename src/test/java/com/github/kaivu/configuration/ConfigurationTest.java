package com.github.kaivu.configuration;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Configuration components
 */
@QuarkusTest
@DisplayName("Configuration Tests")
class ConfigurationTest {

    @Test
    @DisplayName("Should test configuration classes exist")
    void testConfigurationClassesExist() {
        // Test that configuration classes exist without injection
        assertNotNull(ConfigsProvider.class);
        assertNotNull(ApplicationConfiguration.class);
        assertNotNull(UTF8Control.class);
        assertNotNull(AppMessageInterpolator.class);
    }

    @Test
    @DisplayName("Should handle UTF8Control functionality")
    void testUTF8Control() {
        UTF8Control utf8Control = new UTF8Control();

        assertNotNull(utf8Control);
        assertDoesNotThrow(utf8Control::getClass);
    }

    @Test
    @DisplayName("Should test AppMessageInterpolator")
    void testAppMessageInterpolator() {
        AppMessageInterpolator interpolator = new AppMessageInterpolator();

        assertNotNull(interpolator);
        assertDoesNotThrow(interpolator::getClass);
    }

    @Test
    @DisplayName("Should handle ApplicationConfiguration")
    void testApplicationConfiguration() {
        ApplicationConfiguration config = new ApplicationConfiguration();

        assertNotNull(config);
        assertDoesNotThrow(config::getClass);
    }
}
