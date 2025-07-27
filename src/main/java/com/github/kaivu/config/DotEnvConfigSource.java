package com.github.kaivu.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class DotEnvConfigSource implements ConfigSource {
    private final Map<String, String> properties;
    private final String name = "DotEnvConfigSource[.env]";

    public DotEnvConfigSource() {
        this.properties = loadDotEnvFile();
    }

    private Map<String, String> loadDotEnvFile() {
        Map<String, String> props = new HashMap<>();

        // First try to load from classpath (for native image)
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(".env")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    parseEnvContent(reader.lines(), props);
                }
                return props;
            }
        } catch (IOException e) {
            // Fallback to file system
        }

        // Fallback: try to load from file system (for development)
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            try {
                parseEnvContent(Files.lines(envFile), props);
            } catch (IOException e) {
                log.error("Failed to load .env file: {}", e.getMessage());
            }
        }

        return props;
    }

    private void parseEnvContent(java.util.stream.Stream<String> lines, Map<String, String> props) {
        lines.filter(line -> !line.trim().isEmpty())
                .filter(line -> !line.trim().startsWith("#"))
                .filter(line -> line.contains("="))
                .forEach(line -> {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();

                        // Remove quotes if present
                        if ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        props.put(key, value);
                    }
                });
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getOrdinal() {
        // Higher ordinal means higher priority
        // This should override application.yml defaults but not system properties
        return 250;
    }
}
