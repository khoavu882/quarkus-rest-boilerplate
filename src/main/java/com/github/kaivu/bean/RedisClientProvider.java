package com.github.kaivu.bean;

import com.github.kaivu.annotations.RedisProfile;
import com.github.kaivu.services.client.RedisClient;
import com.github.kaivu.services.client.impl.RedisClientImpl;
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
