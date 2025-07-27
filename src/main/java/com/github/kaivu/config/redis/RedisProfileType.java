package com.github.kaivu.config.redis;

import lombok.Getter;

/**
 * Redis profile types for different environments and use cases
 */
@Getter
public enum RedisProfileType {
    /**
     * Default Redis connection (uses main quarkus.redis configuration)
     */
    DEFAULT("default"),

    /**
     * Demo environment Redis connection
     */
    DEMO("demo");

    private final String configKey;

    RedisProfileType(String configKey) {
        this.configKey = configKey;
    }
}
