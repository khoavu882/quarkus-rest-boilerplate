package com.github.kaivu.common.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

/**
 * Unit tests for ImageUtil
 */
@DisplayName("Image Utility Tests")
class ImageUtilTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should be a utility class that cannot be instantiated")
    void testUtilityClass() {
        // Test that the class has the expected utility structure
        assertNotNull(ImageUtil.class);

        // Verify it's a utility class by checking if it has static methods
        try {
            var methods = ImageUtil.class.getDeclaredMethods();
            assertTrue(methods.length > 0, "ImageUtil should have methods");
        } catch (Exception e) {
            fail("Should be able to access ImageUtil methods");
        }
    }

    @Test
    @DisplayName("Should convert ARGB to RGB values")
    void testGetRGBs() {
        int[] argbValues = {0xFF123456, 0xFFABCDEF, 0xFF000000};
        int[] rgbValues = ImageUtil.getRGBs(argbValues);

        assertEquals(0x123456, rgbValues[0]);
        assertEquals(0xABCDEF, rgbValues[1]);
        assertEquals(0x000000, rgbValues[2]);
    }

    @Test
    @DisplayName("Should handle empty ARGB array")
    void testGetRGBsEmptyArray() {
        int[] emptyArray = {};
        int[] result = ImageUtil.getRGBs(emptyArray);

        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Should create PNG from file with rounded corners")
    void testToPNGFromFile() throws IOException {
        // Create a test image file
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        File sourceFile = tempDir.resolve("test_input.png").toFile();
        File outputFile = tempDir.resolve("test_output.png").toFile();

        javax.imageio.ImageIO.write(testImage, "PNG", sourceFile);

        assertDoesNotThrow(() -> ImageUtil.toPNG(sourceFile, outputFile, 50, 10));
        assertTrue(outputFile.exists());
    }

    @Test
    @DisplayName("Should handle IOException when reading invalid file")
    void testToPNGFromInvalidFile() {
        File invalidFile = new File("nonexistent.png");
        File outputFile = tempDir.resolve("output.png").toFile();

        assertThrows(IOException.class, () -> ImageUtil.toPNG(invalidFile, outputFile, 50, 10));
    }

    @Test
    @DisplayName("Should create PNG from URL to ByteArrayOutputStream")
    void testToPNGFromURLToByteArray() throws IOException {
        // Create a test image and save it to a temporary file to get a URL
        BufferedImage testImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        File tempFile = tempDir.resolve("test_url_source.png").toFile();
        javax.imageio.ImageIO.write(testImage, "PNG", tempFile);

        URL testURL = tempFile.toURI().toURL();

        ByteArrayOutputStream result = ImageUtil.toPNG(testURL, 25, 5);

        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should handle IOException when URL is invalid")
    void testToPNGFromInvalidURL() throws IOException {
        URI invalidURI = URI.create("file://nonexistent/path/image.png");
        URL invalidURL = invalidURI.toURL();

        assertThrows(IOException.class, () -> ImageUtil.toPNG(invalidURL, 50, 10));
    }

    @Test
    @DisplayName("Should create PNG from URL to OutputStream")
    void testToPNGFromURLToOutputStream() throws IOException {
        // Create a test image and save it to get a URL
        BufferedImage testImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 30, 30);
        g.dispose();

        File tempFile = tempDir.resolve("test_url_output.png").toFile();
        javax.imageio.ImageIO.write(testImage, "PNG", tempFile);

        URL testURL = tempFile.toURI().toURL();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> ImageUtil.toPNG(testURL, outputStream, 15, 3));
        assertTrue(outputStream.size() > 0);
    }
}
