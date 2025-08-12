package com.github.kaivu.config.bean;

import com.github.kaivu.adapter.out.client.RedisHelper;
import com.github.kaivu.adapter.out.client.impl.RedisHelperImpl;
import com.github.kaivu.config.AppConfiguration;
import com.github.kaivu.config.redis.RedisProfile;
import com.github.kaivu.config.redis.RedisProfileType;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Simplified Redis client provider using Helper pattern only
 * Follows hexagonal architecture and project standards
 */
@ApplicationScoped
public class RedisClientProvider {

    @Inject
    AppConfiguration config;

    /**
     * Default Redis Helper (uses main quarkus.redis configuration)
     */
    @Produces
    @Singleton
    @RedisProfile(RedisProfileType.DEFAULT)
    public RedisHelper defaultRedisHelper(ReactiveRedisDataSource dataSource) {
        return new RedisHelperImpl(dataSource, config);
    }
}
