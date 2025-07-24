package com.github.kaivu.configuration.bean;

import com.github.kaivu.adapter.out.client.RedisClient;
import com.github.kaivu.adapter.out.client.impl.RedisClientImpl;
import com.github.kaivu.configuration.annotations.RedisProfile;
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
