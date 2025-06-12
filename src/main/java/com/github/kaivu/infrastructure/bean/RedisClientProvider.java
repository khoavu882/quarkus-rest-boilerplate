package com.github.kaivu.infrastructure.bean;

import com.github.kaivu.infrastructure.annotations.RedisProfile;
import com.github.kaivu.infrastructure.services.client.RedisClient;
import com.github.kaivu.infrastructure.services.client.impl.RedisClientImpl;
import io.quarkus.redis.client.RedisClientName;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import jakarta.ws.rs.Produces;

public class RedisClientProvider {

    @Produces
    @RedisProfile("demo")
    public RedisClient demoRedisClient(@RedisClientName("demo") ReactiveRedisDataSource dataSource) {
        return new RedisClientImpl(dataSource);
    }
}
