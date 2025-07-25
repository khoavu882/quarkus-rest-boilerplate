package com.github.kaivu.configuration;

import org.eclipse.microprofile.config.ConfigProvider;

public class ConfigsProvider {

    private ConfigsProvider() {
        // Private constructor to hide the implicit public one
    }

    public static final String APP_NAME = ConfigProvider.getConfig().getValue("quarkus.application.name", String.class);

    /*
     * *****************************************************************************
     * Http Filters configurations
     */
    public static final Boolean ENABLE_COMPRESSION = ConfigProvider.getConfig()
            .getOptionalValue("quarkus.http.enable-compression", Boolean.class)
            .orElse(Boolean.TRUE);
    public static final Boolean ENABLE_AUTH_LOGGING = ConfigProvider.getConfig()
            .getOptionalValue("quarkus.http.auth-logging", Boolean.class)
            .orElse(Boolean.TRUE);

    /*
     * *****************************************************************************
     * Database configurations
     */
    public static final String DATABASE_SCHEMA =
            ConfigProvider.getConfig().getValue("quarkus.hibernate-orm.database.default-schema", String.class);

    /*
     * *****************************************************************************
     * MinIO storage configurations
     */
    public static final String MINIO_URL = ConfigProvider.getConfig().getValue("minio.url", String.class);
    public static final String MINIO_ACCESS_KEY = ConfigProvider.getConfig().getValue("minio.access-key", String.class);
    public static final String MINIO_SECRET_KEY = ConfigProvider.getConfig().getValue("minio.secret-key", String.class);
}
