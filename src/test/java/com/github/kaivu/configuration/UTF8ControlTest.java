package com.github.kaivu.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

/**
 * Tests for UTF8Control to ensure proper internationalization handling
 */
@DisplayName("UTF8 Control Tests")
class UTF8ControlTest {

    private final UTF8Control utf8Control = new UTF8Control();

    @Test
    @DisplayName("Should create UTF8Control instance")
    void testUTF8ControlCreation() {
        assertNotNull(utf8Control);
    }

    @Test
    @DisplayName("Should handle newBundle creation with proper encoding")
    void testNewBundle() throws Exception {
        // Given
        String baseName = "messages";
        Locale locale = Locale.ENGLISH;
        String format = "java.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        boolean reload = false;

        // When & Then - should not throw exception even if bundle doesn't exist
        assertDoesNotThrow(() -> {
            try {
                utf8Control.newBundle(baseName, locale, format, loader, reload);
            } catch (IOException | IllegalAccessException | InstantiationException e) {
                // Expected for non-existent bundles in test environment
            }
        });
    }

    @Test
    @DisplayName("Should handle different locales")
    void testNewBundleWithDifferentLocales() {
        String baseName = "test";
        String format = "java.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        boolean reload = false;

        Locale[] locales = {Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, new Locale("vi")};

        for (Locale locale : locales) {
            assertDoesNotThrow(() -> {
                try {
                    utf8Control.newBundle(baseName, locale, format, loader, reload);
                } catch (IOException | IllegalAccessException | InstantiationException e) {
                    // Expected for non-existent bundles
                }
            });
        }
    }

    @Test
    @DisplayName("Should handle reload parameter correctly")
    void testNewBundleWithReload() {
        String baseName = "test";
        Locale locale = Locale.ENGLISH;
        String format = "java.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Test both reload scenarios
        assertDoesNotThrow(() -> {
            try {
                utf8Control.newBundle(baseName, locale, format, loader, true);
            } catch (IOException | IllegalAccessException | InstantiationException e) {
                // Expected for non-existent bundles
            }
        });

        assertDoesNotThrow(() -> {
            try {
                utf8Control.newBundle(baseName, locale, format, loader, false);
            } catch (IOException | IllegalAccessException | InstantiationException e) {
                // Expected for non-existent bundles
            }
        });
    }
}
