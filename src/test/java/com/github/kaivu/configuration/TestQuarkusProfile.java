package com.github.kaivu.configuration;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Test profile for Quarkus tests
 * Configures test-specific settings and overrides
 */
public class TestQuarkusProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.profile", "test",
                "quarkus.datasource.db-kind", "h2",
                "quarkus.datasource.username", "test",
                "quarkus.datasource.password", "test",
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "quarkus.hibernate-orm.database.generation", "drop-and-create",
                "quarkus.hibernate-orm.sql-load-script", "import-test.sql",
                "quarkus.log.level", "INFO",
                "quarkus.log.category.\"com.github.kaivu\".level", "DEBUG");
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }
}
